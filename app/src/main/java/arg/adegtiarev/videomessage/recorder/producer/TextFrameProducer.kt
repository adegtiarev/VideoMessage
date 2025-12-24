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
 * Data for rendering a text frame.
 * Fully describes the UI state at a moment in time.
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

    // Text selection color
    private val selectionColor = Color.rgb(0, 128, 128)

    fun createFrame(data: TextFrameData): Bitmap {
        // 1. Create a bitmap of the original View size
        // Use viewWidth/Height if > 0, otherwise fallback to video dimensions for safety
        val w = if (data.viewWidth > 0) data.viewWidth else VideoRecorderImpl.VIDEO_WIDTH
        val h = if (data.viewHeight > 0) data.viewHeight else VideoRecorderImpl.VIDEO_HEIGHT
        
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background
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

        // 2. Create a full Layout to calculate lines (lightweight operation, text is not drawn)
        // If the text is empty, StaticLayout might crash or create 0 lines. Handle empty text.
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

        // 3. Calculate the visible line range
        val firstVisibleLine = fullTextLayout.getLineForVertical(max(0, data.scrollY))
        // Take lastVisibleLine with a small margin (+h) to cover the screen
        val lastVisibleLine = fullTextLayout.getLineForVertical(max(0, data.scrollY + h))

        val lineStart = fullTextLayout.getLineStart(getSafeLayoutLine(fullTextLayout, firstVisibleLine))
        val lineEnd = fullTextLayout.getLineEnd(getSafeLayoutLine(fullTextLayout, lastVisibleLine))

        val visibleText = safeText.substring(lineStart, lineEnd)
        val spannable = SpannableString(visibleText)

        // 4. Calculate selection for the visible part
        if (data.selectionStart != data.selectionEnd) {
            // Shift absolute selection coordinates relative to the start of the visible text
            val relativeSelStart = data.selectionStart - lineStart
            val relativeSelEnd = data.selectionEnd - lineStart

            // Clamp to the visible text range
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

        // 5. Create a Layout for the visible part
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

        // 6. Draw with smooth scrolling in mind
        canvas.save()
        
        // Calculate Y offset
        val firstVisibleLineTop = fullTextLayout.getLineTop(firstVisibleLine)
        val yOffset = firstVisibleLineTop - data.scrollY // Will be <= 0 or around that
        
        // Translate canvas: padding on X, yOffset on Y + padding on Y (if text in UI starts with padding)
        canvas.translate(data.padding.toFloat(), yOffset.toFloat() + data.padding.toFloat())
        
        visibleLayout.draw(canvas)

        // 7. Draw cursor
        if (data.selectionStart == data.selectionEnd) {
            val relativeCursorPos = data.selectionStart - lineStart
            if (relativeCursorPos >= 0 && relativeCursorPos <= visibleText.length) {
                drawCursor(canvas, visibleLayout, relativeCursorPos)
            }
        }

        canvas.restore()

        // 8. Scale to video size
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

        // Scale Aspect Fit logic (fit entirely)
        var finalWidth = floor((width * widthRatio).toDouble()).toInt()
        var finalHeight = floor((height * widthRatio).toDouble()).toInt()

        if (finalWidth > targetWidth || finalHeight > targetHeight) {
            finalWidth = floor((width * heightRatio).toDouble()).toInt()
            finalHeight = floor((height * heightRatio).toDouble()).toInt()
        }
        
        // Use filter=true for smoothing when downscaling
        return bitmap.scale(finalWidth, finalHeight, filter = true)
    }
}
