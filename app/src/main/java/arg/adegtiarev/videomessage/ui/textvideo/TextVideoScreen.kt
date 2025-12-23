package arg.adegtiarev.videomessage.ui.textvideo

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import arg.adegtiarev.videomessage.R
import arg.adegtiarev.videomessage.ui.components.VideoCreatorTopBar

@Composable
fun TextVideoScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: TextVideoViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Блокируем системную кнопку "Назад" во время записи
    BackHandler(enabled = isRecording) {
        // Можно показать Toast, что идет запись
    }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = uiState.text)) }
    val scrollState = rememberScrollState()

    // Храним результат верстки текста
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val density = LocalDensity.current
    val paddingInPx = with(density) { 16.dp.toPx() }.toInt()

    LaunchedEffect(scrollState.value) {
        viewModel.updateTextState(scrollY = scrollState.value)
    }

    LaunchedEffect(textFieldValue.selection, layoutResult) {
        val layout = layoutResult ?: return@LaunchedEffect
        if (textFieldValue.text.isEmpty()) return@LaunchedEffect
        
        val selectionEnd = textFieldValue.selection.end
        
        val cursorRect = try {
            layout.getCursorRect(selectionEnd)
        } catch (e: Exception) {
            return@LaunchedEffect
        }

        val cursorBottom = cursorRect.bottom
        val cursorTop = cursorRect.top

        val viewportHeight = uiState.viewHeight
        val currentScroll = scrollState.value
        val paddingCursor = 50 

        if (cursorBottom > currentScroll + viewportHeight - paddingCursor) {
            val targetScroll = cursorBottom - viewportHeight + paddingCursor
            scrollState.animateScrollTo(targetScroll.toInt())
        }

        if (cursorTop < currentScroll + paddingCursor) {
            val targetScroll = cursorTop - paddingCursor
            scrollState.animateScrollTo(targetScroll.toInt())
        }
    }

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

    LaunchedEffect(Unit) {
        viewModel.navigateToPlayer.collect { videoPath ->
            onNavigateToPlayer(videoPath)
        }
    }

    Scaffold(
        topBar = {
            VideoCreatorTopBar(
                title = "Create Text Video",
                isRecording = isRecording,
                // Блокируем кнопку "Назад" в тулбаре во время записи
                onBack = { if (!isRecording) onBack() },
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
                    Box {
                        IconButton(onClick = { showSizeMenu = true }) {
                            Text(
                                text = "${uiState.textSizeSp.toInt()}",
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
                                    text = { Text(text = "${size.toInt()} sp", fontSize = if (size > 32f) 32.sp else size.sp) },
                                    onClick = {
                                        viewModel.updateStyle(textSizeSp = size)
                                        showSizeMenu = false
                                    },
                                    trailingIcon = {
                                        if (uiState.textSizeSp == size) {
                                            Icon(painterResource(R.drawable.ic_check), "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showStyleMenu = true }) {
                            Text(
                                text = "A",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = uiState.fontWeight,
                                fontStyle = uiState.fontStyle
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
                                        viewModel.updateStyle(fontWeight = weight, fontStyle = style)
                                        showStyleMenu = false
                                    },
                                    trailingIcon = {
                                        if (uiState.fontWeight == weight && uiState.fontStyle == style) {
                                            Icon(painterResource(R.drawable.ic_check), "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showTextColorMenu = true }) {
                            Icon(painterResource(R.drawable.ic_text_color), "Text Color", tint = uiState.textColor)
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
                                            modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.Gray, CircleShape)
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateStyle(textColor = color)
                                        showTextColorMenu = false
                                    },
                                    trailingIcon = {
                                        if (uiState.textColor == color) {
                                            Icon(painterResource(R.drawable.ic_check), "Selected", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showBgColorMenu = true }) {
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(painterResource(R.drawable.ic_bg_color), "Background Color", tint = uiState.backgroundColor, modifier = Modifier.padding(2.dp))
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
                                            modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.Gray, CircleShape)
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateStyle(backgroundColor = color)
                                        showBgColorMenu = false
                                    },
                                    trailingIcon = {
                                        if (uiState.backgroundColor == color) {
                                            Icon(painterResource(R.drawable.ic_check), "Selected", tint = MaterialTheme.colorScheme.primary)
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
                .background(uiState.backgroundColor)
        ) {
            val textStyle = TextStyle(
                fontSize = uiState.textSizeSp.sp,
                fontWeight = uiState.fontWeight,
                fontStyle = uiState.fontStyle,
                color = uiState.textColor
            )
            
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    viewModel.updateTextState(
                        text = newValue.text,
                        selection = newValue.selection
                    )
                },
                onTextLayout = { result -> layoutResult = result },
                textStyle = textStyle,
                cursorBrush = SolidColor(Color.Green),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .onGloballyPositioned { coordinates ->
                        viewModel.updateTextState(
                             viewWidth = coordinates.size.width,
                             viewHeight = coordinates.parentLayoutCoordinates?.size?.height ?: coordinates.size.height,
                             padding = paddingInPx
                        )
                    },
                decorationBox = { innerTextField ->
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "Start typing...",
                            style = textStyle.copy(color = uiState.textColor.copy(alpha = 0.5f))
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
