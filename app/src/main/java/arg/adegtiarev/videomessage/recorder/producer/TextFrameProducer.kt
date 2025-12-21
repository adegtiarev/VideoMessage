package arg.adegtiarev.videomessage.recorder.producer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
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
    val viewWidth: Int = 1080,  // Ширина TextField на экране
    val viewHeight: Int = 1920, // Высота видимой области TextField
    val textSizePx: Float = 48f,
    val textColor: Int = Color.BLACK,
    val backgroundColor: Int = Color.WHITE,
    val padding: Int = 0
)

class TextFrameProducer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT
    }

    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = 5f
    }

    // Цвет выделения текста (как в EditText)
    private val selectionColor = Color.rgb(0, 128, 128)

    /**
     * Создает Bitmap, симулируя то, что видит пользователь на экране.
     */
    fun createFrame(data: TextFrameData): Bitmap {
        val videoWidth = VideoRecorderImpl.VIDEO_WIDTH
        val videoHeight = VideoRecorderImpl.VIDEO_HEIGHT

        // Создаем битмап исходного размера View, чтобы потом его отмасштабировать
        // Это может быть неоптимально по памяти, но проще для логики с координатами
        var bitmap = Bitmap.createBitmap(data.viewWidth, data.viewHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Заливка фона
        canvas.drawColor(data.backgroundColor)

        // 2. Настройка кисти
        textPaint.textSize = data.textSizePx
        textPaint.color = data.textColor

        val layoutWidth = max(100, data.viewWidth - (data.padding * 2))

        // 3. Создаем полный Layout для расчета строк
        val fullTextLayout = StaticLayout.Builder.obtain(
            data.text,
            0,
            data.text.length,
            textPaint,
            layoutWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()

        // 4. Определяем видимый диапазон строк
        val firstVisibleLine = fullTextLayout.getLineForVertical(max(0, data.scrollY))
        val lastVisibleLine = fullTextLayout.getLineForVertical(max(0, data.scrollY + data.viewHeight))

        val lineStart = fullTextLayout.getLineStart(getSafeLayoutLine(fullTextLayout, firstVisibleLine))
        val lineEnd = fullTextLayout.getLineEnd(getSafeLayoutLine(fullTextLayout, lastVisibleLine))
        
        // Получаем видимый текст
        val visibleText = data.text.substring(lineStart, lineEnd)
        
        // Для правильного рендеринга выделения нам нужно знать позицию относительно видимого куска
        // Мы будем рисовать только видимую часть текста в новый Layout
        
        val spannable = SpannableString(visibleText)
        
        // 5. Применяем выделение
        if (data.selectionStart != data.selectionEnd) {
            // Конвертируем абсолютные позиции выделения в относительные для visibleText
            val relativeSelStart = data.selectionStart - lineStart
            val relativeSelEnd = data.selectionEnd - lineStart
            
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

        // 6. Отрисовка текста
        canvas.save()
        canvas.translate(data.padding.toFloat(), 0f)
        
        // Здесь мы рисуем видимый Layout. В отличие от сдвига полного Layout, это оптимизация.
        // Но нам нужно правильно позиционировать его по вертикали?
        // В старом коде: canvas.translate(codeData.viewCompoundPaddingLeft.toFloat(), 0f) -> colorTextLayout.draw(canvas)
        // Старый код просто рисовал обрезанный текст в начале канваса (0,0), потому что пользователь видит его на экране.
        // Так как мы эмулируем экран, мы просто рисуем текст.
        
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
        return scaleBitmap(bitmap, videoWidth, videoHeight)
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
            // Ignored
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
        
        // Fit Center / Aspect Fit логика (как в старом коде)
        // Старый код: если widthRatio < heightRatio (видео уже), то scale по ширине
        // Но там была странная логика с floor.
        
        // Упростим: Scale Aspect Fit (по меньшей стороне, чтобы вместить всё) или Fill?
        // Старый код делал Scale по ширине, но если результат больше по высоте, то скейлил по высоте.
        // Это Aspect Fit.
        
        var finalWidth = floor((width * widthRatio).toDouble()).toInt()
        var finalHeight = floor((height * widthRatio).toDouble()).toInt()
        
        if (finalWidth > targetWidth || finalHeight > targetHeight) {
            finalWidth = floor((width * heightRatio).toDouble()).toInt()
            finalHeight = floor((height * heightRatio).toDouble()).toInt()
        }
        
        // ВАЖНО: scale возвращает новый bitmap. Старый надо бы recycle, но Android сам справится.
        return bitmap.scale(finalWidth, finalHeight, filter = true)
    }
}
