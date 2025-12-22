package arg.adegtiarev.videomessage.ui.textvideo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import arg.adegtiarev.videomessage.R
import arg.adegtiarev.videomessage.ui.components.VideoCreatorTopBar

@Composable
fun TextVideoScreen(
    onBack: () -> Unit,
    viewModel: TextVideoViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()

    // --- Временное состояние (позже перенесем во ViewModel) ---
    var text by remember { mutableStateOf("") }
    var textSizeSp by remember { mutableFloatStateOf(24f) }
    
    var fontWeight by remember { mutableStateOf(FontWeight.Normal) }
    var fontStyle by remember { mutableStateOf(FontStyle.Normal) }
    
    var textColor by remember { mutableStateOf(Color.Black) }
    var backgroundColor by remember { mutableStateOf(Color.White) }

    // Состояния видимости меню
    var showSizeMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }
    var showTextColorMenu by remember { mutableStateOf(false) }
    var showBgColorMenu by remember { mutableStateOf(false) }

    val availableFontSizes = remember {
        listOf(12f, 14f, 16f, 18f, 20f, 24f, 28f, 32f, 36f, 40f, 48f, 56f, 64f, 72f, 96f)
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
    // ----------------------------------------------------------

    Scaffold(
        topBar = {
            VideoCreatorTopBar(
                title = "Create Text Video",
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
                    // 1. Кнопка размера шрифта
                    Box {
                        IconButton(onClick = { showSizeMenu = true }) {
                            Text(
                                text = "${textSizeSp.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = showSizeMenu,
                            onDismissRequest = { showSizeMenu = false },
                            offset = DpOffset(x = 0.dp, y = 10.dp)
                        ) {
                            availableFontSizes.forEach { size ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${size.toInt()} sp",
                                            fontSize = if (size > 32f) 32.sp else size.sp
                                        )
                                    },
                                    onClick = {
                                        textSizeSp = size
                                        showSizeMenu = false
                                    },
                                    trailingIcon = {
                                        if (textSizeSp == size) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 2. Кнопка стиля шрифта
                    Box {
                        IconButton(onClick = { showStyleMenu = true }) {
                            Text(
                                text = "A",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = fontWeight,
                                fontStyle = fontStyle
                            )
                        }

                        DropdownMenu(
                            expanded = showStyleMenu,
                            onDismissRequest = { showStyleMenu = false },
                            offset = DpOffset(x = 0.dp, y = 10.dp)
                        ) {
                            val styles = listOf(
                                Triple("Normal", FontWeight.Normal, FontStyle.Normal),
                                Triple("Bold", FontWeight.Bold, FontStyle.Normal),
                                Triple("Italic", FontWeight.Normal, FontStyle.Italic),
                                Triple("Bold Italic", FontWeight.Bold, FontStyle.Italic)
                            )
                            
                            styles.forEach { (name, weight, style) ->
                                DropdownMenuItem(
                                    text = { Text(name, fontWeight = weight, fontStyle = style) },
                                    onClick = {
                                        fontWeight = weight
                                        fontStyle = style
                                        showStyleMenu = false
                                    },
                                    trailingIcon = {
                                        if (fontWeight == weight && fontStyle == style) {
                                            Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 3. Кнопка цвета текста
                    Box {
                        IconButton(onClick = { showTextColorMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_text_color),
                                contentDescription = "Text Color",
                                tint = textColor
                            )
                        }

                        DropdownMenu(
                            expanded = showTextColorMenu,
                            onDismissRequest = { showTextColorMenu = false },
                            offset = DpOffset(x = 0.dp, y = 10.dp)
                        ) {
                            availableColors.forEach { (name, color) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(1.dp, Color.Gray, CircleShape)
                                        )
                                    },
                                    onClick = {
                                        textColor = color
                                        showTextColorMenu = false
                                    },
                                    trailingIcon = {
                                        if (textColor == color) {
                                            Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 4. Кнопка цвета фона
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
                                    tint = backgroundColor,
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
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(1.dp, Color.Gray, CircleShape)
                                        )
                                    },
                                    onClick = {
                                        backgroundColor = color
                                        showBgColorMenu = false
                                    },
                                    trailingIcon = {
                                        if (backgroundColor == color) {
                                            Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    fontSize = textSizeSp.sp,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    color = textColor
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = backgroundColor,
                    unfocusedContainerColor = backgroundColor,
                    disabledContainerColor = backgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = { Text("Start typing...") }
            )
        }
    }
}
