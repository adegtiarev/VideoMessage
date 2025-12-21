package arg.adegtiarev.videomessage.recorder

import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaRecorder
import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface VideoRecorder {
    fun start(outputFile: File)
    fun stop()
    fun updateFrame(bitmap: Bitmap)
    val isRecording: Boolean
}

@Singleton
class VideoRecorderImpl @Inject constructor() : VideoRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var inputSurface: Surface? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var currentBitmap: Bitmap? = null

    @Volatile
    override var isRecording: Boolean = false
        private set

    override fun start(outputFile: File) {
        if (isRecording) return

        // Setup MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setVideoFrameRate(FRAME_RATE)
            setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT)
            setVideoEncodingBitRate(VIDEO_WIDTH * VIDEO_HEIGHT * 5)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setOutputFile(outputFile.absolutePath)
            prepare()
        }

        inputSurface = mediaRecorder?.surface
        mediaRecorder?.start()
        isRecording = true

        startLoop()
    }

    private fun startLoop() {
        recordingJob = scope.launch {
            var lastFrameTime = 0L
            while (isActive && isRecording) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFrameTime >= FRAME_DELAY_TIME) {
                    val bitmap = currentBitmap
                    if (bitmap != null) {
                        drawToSurface(bitmap)
                    } else {
                        // Если битмапа еще нет, можно нарисовать фон или пропустить
                        drawEmptyFrame()
                    }
                    lastFrameTime = currentTime
                }
                // Небольшая задержка, чтобы не грузить CPU в пустом цикле, 
                // но достаточно частая для точности тайминга
                delay(5) 
            }
        }
    }

    private fun drawToSurface(bitmap: Bitmap) {
        inputSurface?.let { surface ->
            if (!surface.isValid) return
            try {
                val canvas = surface.lockCanvas(null)
                // Центрируем битмап или масштабируем
                // Для простоты пока рисуем как есть, предполагая, что генератор кадров 
                // создает битмап нужного размера (VIDEO_WIDTH x VIDEO_HEIGHT)
                canvas.drawColor(Color.WHITE) // Очистка фона
                
                // Расчет координат для центрирования, если размеры не совпадают
                val left = (VIDEO_WIDTH - bitmap.width) / 2f
                val top = (VIDEO_HEIGHT - bitmap.height) / 2f
                
                canvas.drawBitmap(bitmap, left, top, null)
                surface.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun drawEmptyFrame() {
        inputSurface?.let { surface ->
            if (!surface.isValid) return
            try {
                val canvas = surface.lockCanvas(null)
                canvas.drawColor(Color.WHITE)
                surface.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun updateFrame(bitmap: Bitmap) {
        // Обновляем текущий кадр. 
        // В продакшене стоит рассмотреть копирование битмапа, если он изменяется снаружи.
        // Но для производительности часто передают новый immutable экземпляр.
        currentBitmap = bitmap
    }

    override fun stop() {
        if (!isRecording) return
        
        isRecording = false
        recordingJob?.cancel()
        
        try {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
        } catch (e: Exception) {
            // Stop может упасть, если видео слишком короткое или не было данных
            e.printStackTrace()
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            inputSurface?.release()
            inputSurface = null
            currentBitmap = null
        }
    }

    companion object {
        const val VIDEO_WIDTH = 960
        const val VIDEO_HEIGHT = 1280
        const val FRAME_RATE = 20
        const val FRAME_DELAY_TIME = 1000L / FRAME_RATE
    }
}
