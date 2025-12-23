package arg.adegtiarev.videomessage.recorder.producer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import androidx.core.graphics.scale
import arg.adegtiarev.videomessage.recorder.VideoRecorderImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Данные для отрисовки кадра текста.
 * Полностью описывают состояние UI в момент времени.
 */
data class TextFrameData(
    val text: String,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val scrollY: Int = 0,
    val viewWidth: Int = 1080,
    val viewHeight: Int = 1920,
    val textSizePx: Float = 48f,
    val textColor: Int = Color.BLACK,
    val backgroundColor: Int = Color.WHITE,
    val padding: Int = 0,
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

class TextFrameProducer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = 5f
    }

    // Цвет выделения текста
    private val selectionColor = Color.rgb(0, 128, 128)

    fun createFrame(data: TextFrameData): Bitmap {
        // 1. Создаем битмап исходного размера View
        // Используем viewWidth/Height, если они > 0, иначе фоллбек на видео размеры (для безопасности)
        val w = if (data.viewWidth > 0) data.viewWidth else VideoRecorderImpl.VIDEO_WIDTH
        val h = if (data.viewHeight > 0) data.viewHeight else VideoRecorderImpl.VIDEO_HEIGHT
        
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Заливка фона
        canvas.drawColor(data.backgroundColor)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = data.textSizePx
            color = data.textColor
            val style = when {
                data.isBold && data.isItalic -> Typeface.BOLD_ITALIC
                data.isBold -> Typeface.BOLD
                data.isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            typeface = Typeface.create(Typeface.DEFAULT, style)
        }

        val layoutWidth = max(100, w - (data.padding * 2))

        // 2. Создаем полный Layout для расчета строк (легкая операция, текст не рисуется)
        // Если текст пустой, StaticLayout может упасть или создать 0 линий. Обработаем пустой текст.
        val safeText = if (data.text.isEmpty()) " " else data.text
        
        val fullTextLayout = StaticLayout.Builder.obtain(
            safeText,
            0,
            safeText.length,
            textPaint,
            layoutWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()

        // 3. Вычисляем видимый диапазон строк
        val firstVisibleLine = fullTextLayout.getLineForVertical(max(0, data.scrollY))
        // lastVisibleLine берем чуть с запасом (+ h), чтобы точно покрыть экран
        val lastVisibleLine = fullTextLayout.getLineForVertical(max(0, data.scrollY + h))

        val lineStart = fullTextLayout.getLineStart(getSafeLayoutLine(fullTextLayout, firstVisibleLine))
        val lineEnd = fullTextLayout.getLineEnd(getSafeLayoutLine(fullTextLayout, lastVisibleLine))

        val visibleText = safeText.substring(lineStart, lineEnd)
        val spannable = SpannableString(visibleText)

        // 4. Расчет выделения для видимой части
        if (data.selectionStart != data.selectionEnd) {
            // Сдвигаем абсолютные координаты выделения относительно начала видимого текста
            val relativeSelStart = data.selectionStart - lineStart
            val relativeSelEnd = data.selectionEnd - lineStart

            // Ограничиваем диапазоном видимого текста
            val start = relativeSelStart.coerceIn(0, visibleText.length)
            val end = relativeSelEnd.coerceIn(0, visibleText.length)

            if (start != end) {
                val min = minOf(start, end)
                val max = maxOf(start, end)
                spannable.setSpan(
                    BackgroundColorSpan(selectionColor),
                    min,
                    max,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }

        // 5. Создаем Layout для видимой части
        val visibleLayout = StaticLayout.Builder.obtain(
            spannable,
            0,
            spannable.length,
            textPaint,
            layoutWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()

        // 6. Отрисовка с учетом плавного скролла
        canvas.save()
        
        // Вычисляем смещение по Y
        val firstVisibleLineTop = fullTextLayout.getLineTop(firstVisibleLine)
        val yOffset = firstVisibleLineTop - data.scrollY // Будет <= 0 или около того
        
        // Смещаем канвас: padding по X, yOffset по Y + padding по Y (если в UI текст начинается с паддинга)
        canvas.translate(data.padding.toFloat(), yOffset.toFloat() + data.padding.toFloat())
        
        visibleLayout.draw(canvas)

        // 7. Отрисовка курсора
        if (data.selectionStart == data.selectionEnd) {
            val relativeCursorPos = data.selectionStart - lineStart
            if (relativeCursorPos >= 0 && relativeCursorPos <= visibleText.length) {
                drawCursor(canvas, visibleLayout, relativeCursorPos)
            }
        }

        canvas.restore()

        // 8. Масштабирование до размера видео
        return scaleBitmap(bitmap, VideoRecorderImpl.VIDEO_WIDTH, VideoRecorderImpl.VIDEO_HEIGHT)
    }

    private fun drawCursor(canvas: Canvas, layout: Layout, position: Int) {
        val safePosition = position.coerceIn(0, layout.text.length)
        try {
            val line = layout.getLineForOffset(safePosition)
            val top = layout.getLineTop(line).toFloat()
            val bottom = layout.getLineBottom(line).toFloat()
            val x = layout.getPrimaryHorizontal(safePosition)
            canvas.drawLine(x, top, x, bottom, cursorPaint)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun getSafeLayoutLine(layout: Layout, line: Int): Int {
        if (line < 0) return 0
        return min(line, layout.lineCount - 1)
    }

    private fun scaleBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val widthRatio = targetWidth.toFloat() / width.toFloat()
        val heightRatio = targetHeight.toFloat() / height.toFloat()

        // Логика Scale Aspect Fit (вписать целиком)
        var finalWidth = floor((width * widthRatio).toDouble()).toInt()
        var finalHeight = floor((height * widthRatio).toDouble()).toInt()

        if (finalWidth > targetWidth || finalHeight > targetHeight) {
            finalWidth = floor((width * heightRatio).toDouble()).toInt()
            finalHeight = floor((height * heightRatio).toDouble()).toInt()
        }
        
        // Используем filter=true для сглаживания при уменьшении
        return bitmap.scale(finalWidth, finalHeight, filter = true)
    }
}
