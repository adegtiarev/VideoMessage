package arg.adegtiarev.videomessage.ui.textvideo

import arg.adegtiarev.videomessage.recorder.VideoRecorder
import arg.adegtiarev.videomessage.ui.BaseVideoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TextVideoViewModel @Inject constructor(
    videoRecorder: VideoRecorder
) : BaseVideoViewModel(videoRecorder) {

    // TODO: Add state for text input (e.g., StateFlow<String>)

    override fun startRecording() {
        // TODO: Get output file path
        // videoRecorder.start(outputFile)
        // TODO: Start frame generation loop
    }

    override fun stopRecording() {
        videoRecorder.stop()
        // TODO: Stop frame generation, save to Room, etc.
    }
}
