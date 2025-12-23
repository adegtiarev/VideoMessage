package arg.adegtiarev.videomessage.recorder.producer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.scale
import arg.adegtiarev.videomessage.recorder.VideoRecorderImpl
import javax.inject.Inject
import kotlin.math.floor

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
    val backgroundColor: Int = Color.WHITE,
    val viewWidth: Int = 0,
    val viewHeight: Int = 0
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
        // 1. Создаем битмап исходного размера View
        // Если размеры не переданы (0), используем размеры видео как фоллбек
        val w = if (data.viewWidth > 0) data.viewWidth else VideoRecorderImpl.VIDEO_WIDTH
        val h = if (data.viewHeight > 0) data.viewHeight else VideoRecorderImpl.VIDEO_HEIGHT

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 2. Заливка фона
        canvas.drawColor(data.backgroundColor)

        // 3. Отрисовка всех линий
        // Рисуем как есть, так как координаты путей (Path) соответствуют размерам w x h (view dimensions)
        for (line in data.lines) {
            paint.color = line.color
            paint.strokeWidth = line.strokeWidth
            canvas.drawPath(line.path, paint)
        }
        
        // 4. Масштабируем до размера видео, используя ту же логику, что и в TextFrameProducer
        return scaleBitmap(bitmap, VideoRecorderImpl.VIDEO_WIDTH, VideoRecorderImpl.VIDEO_HEIGHT)
    }

    private fun scaleBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val widthRatio = targetWidth.toFloat() / width.toFloat()
        val heightRatio = targetHeight.toFloat() / height.toFloat()

        // Логика Scale Aspect Fit (вписать целиком)
        // Вычисляем размеры при масштабировании по ширине
        var finalWidth = floor((width * widthRatio).toDouble()).toInt()
        var finalHeight = floor((height * widthRatio).toDouble()).toInt()

        // Если при этом высота выходит за рамки целевой, то масштабируем по высоте
        if (finalWidth > targetWidth || finalHeight > targetHeight) {
            finalWidth = floor((width * heightRatio).toDouble()).toInt()
            finalHeight = floor((height * heightRatio).toDouble()).toInt()
        }
        
        // Используем filter=true для сглаживания при уменьшении
        return bitmap.scale(finalWidth, finalHeight, filter = true)
    }
}
