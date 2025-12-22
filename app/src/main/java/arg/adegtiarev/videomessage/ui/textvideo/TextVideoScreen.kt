package arg.adegtiarev.videomessage.ui.textvideo

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
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.text.font.FontWeight
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
    var isBold by remember { mutableStateOf(false) }
    var textColor by remember { mutableStateOf(Color.Black) }
    var backgroundColor by remember { mutableStateOf(Color.White) }
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
                    IconButton(onClick = {
                        textSizeSp = if (textSizeSp >= 48f) 16f else textSizeSp + 4f
                    }) {
                        Text(
                            text = "${textSizeSp.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { isBold = !isBold }) {
                        if (isBold) {
                            Icon(
                                painter = painterResource(R.drawable.ic_format_bold),
                                contentDescription = "Bold"
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_text_style),
                                contentDescription = "Normal"
                            )
                        }
                    }

                    IconButton(onClick = {
                        textColor = if (textColor == Color.Black) Color.Red else Color.Black
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_text_color),
                            contentDescription = "Text Color",
                            tint = textColor
                        )
                    }

                    IconButton(onClick = {
                        backgroundColor = if (backgroundColor == Color.White) Color.Yellow else Color.White
                    }) {
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
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
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
