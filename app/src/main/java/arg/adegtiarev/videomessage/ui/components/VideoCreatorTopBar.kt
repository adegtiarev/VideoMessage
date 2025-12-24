package arg.adegtiarev.videomessage.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import arg.adegtiarev.videomessage.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCreatorTopBar(
    title: String,
    isRecording: Boolean,
    onBack: () -> Unit,
    onToggleRecording: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleRecording) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_record),
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
