package arg.adegtiarev.videomessage.ui.drawingvideo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import arg.adegtiarev.videomessage.R
import arg.adegtiarev.videomessage.ui.components.VideoCreatorTopBar

@Composable
fun DrawingVideoScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: DrawingVideoViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Состояния видимости меню
    var showBrushSizeMenu by remember { mutableStateOf(false) }
    var showBrushColorMenu by remember { mutableStateOf(false) }
    var showBgColorMenu by remember { mutableStateOf(false) }

    val brushSizes = remember {
        listOf(5f, 10f, 15f, 20f, 30f, 40f, 50f, 60f, 80f, 100f)
    }

    val availableColors = remember {
        listOf(
            "Black" to Color.Black,
            "Dark Gray" to Color.DarkGray,
            "Gray" to Color.Gray,
            "Light Gray" to Color.LightGray,
            "White" to Color.White,
            "Red" to Color.Red,
            "Green" to Color.Green,
            "Blue" to Color.Blue,
            "Yellow" to Color.Yellow,
            "Cyan" to Color.Cyan,
            "Magenta" to Color.Magenta
        )
    }
    
    // Навигация
    LaunchedEffect(Unit) {
        viewModel.navigateToPlayer.collect { videoPath ->
            onNavigateToPlayer(videoPath)
        }
    }

    Scaffold(
        topBar = {
            VideoCreatorTopBar(
                title = "Create Drawing Video",
                isRecording = isRecording,
                onBack = onBack,
                onToggleRecording = viewModel::onToggleRecording
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Размер кисти
                    Box {
                        IconButton(onClick = { showBrushSizeMenu = true }) {
                            Icon(painterResource(R.drawable.ic_brush_size), contentDescription = "Brush Size")
                        }
                        DropdownMenu(
                            expanded = showBrushSizeMenu,
                            onDismissRequest = { showBrushSizeMenu = false },
                            offset = DpOffset(x = 0.dp, y = 10.dp)
                        ) {
                            brushSizes.forEach { brushSize ->
                                DropdownMenuItem(
                                    text = { Text("${brushSize.toInt()} px") },
                                    leadingIcon = {
                                        val iconColor = MaterialTheme.colorScheme.onSurface
                                        Canvas(modifier = Modifier.size(24.dp)) {
                                            val maxRadius = this.size.minDimension / 2
                                            val radius = (brushSize / 2).coerceAtMost(maxRadius)
                                            drawCircle(color = iconColor, radius = radius)
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateBrush(thickness = brushSize)
                                        showBrushSizeMenu = false
                                    },
                                    trailingIcon = {
                                        if (uiState.brushThickness == brushSize) {
                                            Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 2. Цвет кисти
                    Box {
                        IconButton(onClick = { showBrushColorMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_palette),
                                contentDescription = "Brush Color",
                                tint = uiState.brushColor
                            )
                        }
                        DropdownMenu(
                            expanded = showBrushColorMenu,
                            onDismissRequest = { showBrushColorMenu = false },
                            offset = DpOffset(x = 0.dp, y = 10.dp)
                        ) {
                            availableColors.forEach { (name, color) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    leadingIcon = {
                                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.Gray, CircleShape))
                                    },
                                    onClick = {
                                        viewModel.updateBrush(color = color)
                                        showBrushColorMenu = false
                                    },
                                    trailingIcon = {
                                        if (uiState.brushColor == color) {
                                            Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 3. Цвет фона
                    Box {
                        IconButton(onClick = { showBgColorMenu = true }) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_bg_color),
                                    contentDescription = "Background Color",
                                    tint = uiState.backgroundColor,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showBgColorMenu,
                            onDismissRequest = { showBgColorMenu = false },
                            offset = DpOffset(x = 0.dp, y = 10.dp)
                        ) {
                            availableColors.forEach { (name, color) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    leadingIcon = {
                                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.Gray, CircleShape))
                                    },
                                    onClick = {
                                        viewModel.updateBackgroundColor(color)
                                        showBgColorMenu = false
                                    },
                                    trailingIcon = {
                                        if (uiState.backgroundColor == color) {
                                            Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 4. Очистить
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(painterResource(R.drawable.ic_clear_all), contentDescription = "Clear Canvas")
                    }
                }
            }
        }
    ) { paddingValues ->
        
        // Холст для рисования
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(uiState.backgroundColor)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.onDragStart(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            viewModel.onDrag(change.position)
                        },
                        onDragEnd = {
                            viewModel.onDragEnd()
                        }
                    )
                }
        ) {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeCap = android.graphics.Paint.Cap.ROUND
                    strokeJoin = android.graphics.Paint.Join.ROUND
                    isAntiAlias = true
                }

                // Рисуем уже завершенные линии
                uiState.lines.forEach { line ->
                    paint.color = line.color
                    paint.strokeWidth = line.strokeWidth
                    canvas.nativeCanvas.drawPath(line.path, paint)
                }

                // Рисуем текущую линию
                uiState.currentLine?.let { line ->
                    paint.color = line.color
                    paint.strokeWidth = line.strokeWidth
                    canvas.nativeCanvas.drawPath(line.path, paint)
                }
            }
        }
    }
}
