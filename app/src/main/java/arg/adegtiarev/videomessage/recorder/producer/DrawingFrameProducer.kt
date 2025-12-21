package arg.adegtiarev.videomessage.recorder.producer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import arg.adegtiarev.videomessage.recorder.VideoRecorderImpl
import javax.inject.Inject

/**
 * Данные об одной нарисованной линии.
 */
data class DrawingLine(
    val path: Path,
    val color: Int,
    val strokeWidth: Float
)

/**
 * Данные, необходимые для отрисовки одного кадра с рисунком.
 */
data class DrawingFrameData(
    val lines: List<DrawingLine>,
    val backgroundColor: Int = Color.WHITE
)

class DrawingFrameProducer @Inject constructor() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /**
     * Создает Bitmap на основе данных рисунка.
     */
    fun createFrame(data: DrawingFrameData): Bitmap {
        val width = VideoRecorderImpl.VIDEO_WIDTH
        val height = VideoRecorderImpl.VIDEO_HEIGHT

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Заливка фона
        canvas.drawColor(data.backgroundColor)

        // 2. Отрисовка всех линий
        for (line in data.lines) {
            paint.color = line.color
            paint.strokeWidth = line.strokeWidth
            canvas.drawPath(line.path, paint)
        }

        return bitmap
    }
}
