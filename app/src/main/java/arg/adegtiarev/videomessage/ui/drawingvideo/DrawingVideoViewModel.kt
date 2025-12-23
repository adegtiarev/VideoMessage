package arg.adegtiarev.videomessage.ui.drawingvideo

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import arg.adegtiarev.videomessage.recorder.VideoRecorder
import arg.adegtiarev.videomessage.recorder.producer.DrawingFrameData
import arg.adegtiarev.videomessage.recorder.producer.DrawingFrameProducer
import arg.adegtiarev.videomessage.recorder.producer.DrawingLine
import arg.adegtiarev.videomessage.ui.BaseVideoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DrawingUiState(
    val brushColor: Color = Color.Black,
    val brushThickness: Float = 10f,
    val backgroundColor: Color = Color.White,
    val lines: List<DrawingLine> = emptyList(),
    val viewWidth: Int = 0,
    val viewHeight: Int = 0
)

@HiltViewModel
class DrawingVideoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    videoRecorder: VideoRecorder,
    private val frameProducer: DrawingFrameProducer
) : BaseVideoViewModel(videoRecorder) {

    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigateToPlayer = MutableSharedFlow<String>()
    val navigateToPlayer = _navigateToPlayer.asSharedFlow()

    private var activeLine: DrawingLine? = null
    private var currentOutputFile: File? = null

    fun updateCanvasSize(width: Int, height: Int) {
        _uiState.update { it.copy(viewWidth = width, viewHeight = height) }
    }

    fun updateBrush(color: Color? = null, thickness: Float? = null) {
        _uiState.update { 
            it.copy(
                brushColor = color ?: it.brushColor, 
                brushThickness = thickness ?: it.brushThickness
            ) 
        }
    }

    fun updateBackgroundColor(color: Color) {
        _uiState.update { it.copy(backgroundColor = color) }
        processFrame()
    }

    fun clearCanvas() {
        _uiState.update { it.copy(lines = emptyList()) }
        activeLine = null
        processFrame()
    }

    fun onDragStart(startPoint: Offset) {
        val currentState = _uiState.value
        val path = android.graphics.Path().apply {
            moveTo(startPoint.x, startPoint.y)
        }
        
        activeLine = DrawingLine(
            path = path,
            color = currentState.brushColor.toArgb(),
            strokeWidth = currentState.brushThickness
        )
        
        processFrame()
    }

    fun onDrag(newPoint: Offset) {
        val line = activeLine ?: return
        line.path.lineTo(newPoint.x, newPoint.y)
        processFrame()
    }

    fun onDragEnd() {
        val line = activeLine ?: return
        
        _uiState.update { 
            it.copy(lines = it.lines + line) 
        }
        activeLine = null
        processFrame()
    }

    private fun processFrame() {
        if (!isRecording.value) return

        viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            
            val allLines = if (activeLine != null) {
                state.lines + activeLine!!
            } else {
                state.lines
            }

            val frameData = DrawingFrameData(
                lines = allLines,
                backgroundColor = state.backgroundColor.toArgb(),
                viewWidth = state.viewWidth,
                viewHeight = state.viewHeight
            )

            val bitmap = frameProducer.createFrame(frameData)
            videoRecorder.updateFrame(bitmap)
        }
    }

    override fun startRecording() {
        val fileName = "VIDEO_DRAW_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"
        val outputFile = File(context.filesDir, fileName)
        currentOutputFile = outputFile

        videoRecorder.start(outputFile)
        processFrame()
    }

    override fun stopRecording() {
        videoRecorder.stop()
        currentOutputFile?.let { file ->
            viewModelScope.launch {
                _navigateToPlayer.emit(file.name)
            }
        }
    }
}
