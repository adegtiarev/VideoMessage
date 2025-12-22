package arg.adegtiarev.videomessage.ui.textvideo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import arg.adegtiarev.videomessage.ui.components.VideoCreatorTopBar

@Composable
fun TextVideoScreen(
    onBack: () -> Unit,
    viewModel: TextVideoViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()

    Scaffold(
        topBar = {
            VideoCreatorTopBar(
                title = "Create Text Video",
                isRecording = isRecording,
                onBack = onBack,
                onToggleRecording = viewModel::onToggleRecording
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Create Text Video Screen")
        }
    }
}
