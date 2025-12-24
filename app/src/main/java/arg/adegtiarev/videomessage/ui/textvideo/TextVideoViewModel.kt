package arg.adegtiarev.videomessage.ui.textvideo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewModelScope
import arg.adegtiarev.videomessage.data.local.VideoType
import arg.adegtiarev.videomessage.domain.VideoRepository
import arg.adegtiarev.videomessage.recorder.VideoRecorder
import arg.adegtiarev.videomessage.recorder.producer.TextFrameData
import arg.adegtiarev.videomessage.recorder.producer.TextFrameProducer
import arg.adegtiarev.videomessage.ui.BaseVideoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class TextVideoUiState(
    val text: String = "",
    val textSizeSp: Float = 24f,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontStyle: FontStyle = FontStyle.Normal,
    val textColor: Color = Color.Black,
    val backgroundColor: Color = Color.White,
    val selection: TextRange = TextRange.Zero,
    val scrollY: Int = 0,
    val viewWidth: Int = 1080,
    val viewHeight: Int = 1920,
    val padding: Int = 0
)

@HiltViewModel
class TextVideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    videoRecorder: VideoRecorder,
    private val frameProducer: TextFrameProducer
) : BaseVideoViewModel(videoRecorder) {

    private val _uiState = MutableStateFlow(TextVideoUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigateToPlayer = MutableSharedFlow<String>()
    val navigateToPlayer = _navigateToPlayer.asSharedFlow()

    private var currentOutputFile: File? = null

    // Variables to fix the view dimensions during recording
    private var recordingViewWidth: Int = 0
    private var recordingViewHeight: Int = 0

    fun updateTextState(
        text: String? = null,
        selection: TextRange? = null,
        scrollY: Int? = null,
        viewWidth: Int? = null,
        viewHeight: Int? = null,
        padding: Int? = null
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                text = text ?: currentState.text,
                selection = selection ?: currentState.selection,
                scrollY = scrollY ?: currentState.scrollY,
                viewWidth = viewWidth ?: currentState.viewWidth,
                viewHeight = viewHeight ?: currentState.viewHeight,
                padding = padding ?: currentState.padding
            )
        }
        // We don't generate a frame if recording is not active
        if (isRecording.value) {
            processFrame()
        }
    }

    fun updateStyle(
        textSizeSp: Float? = null,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null,
        textColor: Color? = null,
        backgroundColor: Color? = null
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                textSizeSp = textSizeSp ?: currentState.textSizeSp,
                fontWeight = fontWeight ?: currentState.fontWeight,
                fontStyle = fontStyle ?: currentState.fontStyle,
                textColor = textColor ?: currentState.textColor,
                backgroundColor = backgroundColor ?: currentState.backgroundColor
            )
        }
        if (isRecording.value) {
            processFrame()
        }
    }

    private fun processFrame() {
        if (!isRecording.value) return

        viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            val frameData = TextFrameData(
                text = state.text,
                selectionStart = state.selection.start,
                selectionEnd = state.selection.end,
                scrollY = state.scrollY,
                // Use fixed dimensions
                viewWidth = recordingViewWidth,
                viewHeight = recordingViewHeight,
                // Approximation, should be passed from UI
                textSizePx = state.textSizeSp * 3,
                textColor = state.textColor.toArgb(),
                backgroundColor = state.backgroundColor.toArgb(),
                isBold = state.fontWeight == FontWeight.Bold,
                isItalic = state.fontStyle == FontStyle.Italic,
                padding = state.padding
            )
            
            val bitmap = frameProducer.createFrame(frameData)
            videoRecorder.updateFrame(bitmap)
        }
    }

    override fun startRecording() {
        // Fix dimensions at the start of recording
        val currentState = _uiState.value
        recordingViewWidth = currentState.viewWidth
        recordingViewHeight = currentState.viewHeight

        val outputFile = videoRepository.createNewVideoFile("TEXT")
        currentOutputFile = outputFile
        
        videoRecorder.start(outputFile)
        
        // Immediately record the first frame with fixed dimensions
        processFrame()
    }

    override fun stopRecording() {
        videoRecorder.stop()
        
        // Reset fixed dimensions
        recordingViewWidth = 0
        recordingViewHeight = 0

        currentOutputFile?.let { file ->
            viewModelScope.launch {
                videoRepository.saveVideo(file, VideoType.TEXT)
                _navigateToPlayer.emit(file.name)
            }
        }
    }
}
