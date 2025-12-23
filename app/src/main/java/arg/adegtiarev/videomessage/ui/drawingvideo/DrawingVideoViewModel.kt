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
    // Список завершенных линий
    val lines: List<DrawingLine> = emptyList(),
    // Текущая линия (которую рисуем прямо сейчас)
    val currentLine: DrawingLine? = null
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

    private var currentOutputFile: File? = null

    // Настройки кисти
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
        processFrame() // Обновляем кадр сразу, даже если не рисуем
    }

    fun clearCanvas() {
        _uiState.update { it.copy(lines = emptyList(), currentLine = null) }
        processFrame()
    }

    // Обработка рисования
    fun onDragStart(startPoint: Offset) {
        val currentState = _uiState.value
        val path = android.graphics.Path().apply {
            moveTo(startPoint.x, startPoint.y)
        }
        val newLine = DrawingLine(
            path = path,
            color = currentState.brushColor.toArgb(),
            strokeWidth = currentState.brushThickness
        )
        
        _uiState.update { it.copy(currentLine = newLine) }
        processFrame()
    }

    fun onDrag(newPoint: Offset) {
        val currentLine = _uiState.value.currentLine ?: return
        
        // Модифицируем Path (он мутабельный, но для Compose обновления 
        // нам нужно обновить ссылку на State или вызвать перерисовку)
        currentLine.path.lineTo(newPoint.x, newPoint.y)
        
        // Форсим обновление стейта, чтобы Compose увидел изменение
        // (хотя объект currentLine тот же, Flow испускает новое значение data class)
        _uiState.update { it.copy(currentLine = currentLine) }
        
        processFrame()
    }

    fun onDragEnd() {
        val currentLine = _uiState.value.currentLine ?: return
        
        _uiState.update { 
            it.copy(
                lines = it.lines + currentLine,
                currentLine = null
            ) 
        }
        processFrame()
    }

    private fun processFrame() {
        if (!isRecording.value) return

        viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            
            // Собираем все линии для кадра: завершенные + текущая (если есть)
            val allLines = if (state.currentLine != null) {
                state.lines + state.currentLine
            } else {
                state.lines
            }

            val frameData = DrawingFrameData(
                lines = allLines,
                backgroundColor = state.backgroundColor.toArgb()
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
