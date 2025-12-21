package arg.adegtiarev.liveletters.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.*
import android.text.style.BackgroundColorSpan
import kotlin.math.max
import kotlin.math.min
import kotlin.math.floor
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

class TextBitmapMaker(
    private val backgroundColor: Int,
    paint: TextPaint,
    private val width: Int,
    private val height: Int
) {
    private val textCodePaint: TextPaint = TextPaint()
    private val selectionColor: Int

    init {
        textCodePaint.set(paint)
        selectionColor = Color.rgb(0, 128, 128) // get selection color from EditText
    }

    fun createFrame(codeData: FrameData): Bitmap {
        var bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        val layoutWidth = codeData.viewWidth - codeData.viewCompoundPaddingLeft

        // create full text layout
        // создаем StaticLayout по полному тексту, чтобы вычислить видимую часть текста и правильно расчитать видимые номера строк
        val fullTextLayout = StaticLayout(
            codeData.text,
            textCodePaint,
            layoutWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            false
        )
        val firstVisibleLine = fullTextLayout.getLineForVertical(max(0, codeData.scrollY))
        val lastVisibleLine = fullTextLayout.getLineForVertical(max(0, codeData.scrollY + height))

        val visibleLinesCount = lastVisibleLine - firstVisibleLine
        var parseStart = 0
        if (firstVisibleLine > 0 && firstVisibleLine > visibleLinesCount) {
            val parseLine = firstVisibleLine - visibleLinesCount
            parseStart = fullTextLayout.getLineStart(getSafeLayoutLine(fullTextLayout, parseLine))
        }

        val lineStart = fullTextLayout.getLineStart(getSafeLayoutLine(fullTextLayout, firstVisibleLine))
        val lineEnd = fullTextLayout.getLineEnd(getSafeLayoutLine(fullTextLayout, lastVisibleLine))
        val visibleText = codeData.text.substring(lineStart, lineEnd)
        val parseText = codeData.text.substring(parseStart, lineEnd)

        // draw text
        // отправляем видимый таекст  StaticLayout
        val colorTextLayout = createColorTextLayout(parseText, visibleText, codeData, layoutWidth, lineStart)
        canvas.translate(codeData.viewCompoundPaddingLeft.toFloat(), 0f)
        colorTextLayout.draw(canvas)
        canvas.translate(-codeData.viewCompoundPaddingLeft.toFloat(), 0f)

        // draw cursor
        val cursorPaint = Paint()
        cursorPaint.color = Color.GREEN
        cursorPaint.strokeWidth = 4f
        val cursorTextPosition = codeData.selectionEnd - lineStart
        if (cursorTextPosition > 0) {
            val lineWithCursor = colorTextLayout.getLineForOffset(cursorTextPosition)
            if (lineWithCursor + firstVisibleLine < lastVisibleLine) {
                val y = colorTextLayout.getLineBottom(lineWithCursor).toFloat()
                val x = colorTextLayout.getPrimaryHorizontal(cursorTextPosition) + codeData.viewCompoundPaddingLeft
                val top = colorTextLayout.getLineTop(lineWithCursor).toFloat()
                canvas.drawLine(x, y, x, top, cursorPaint)
            }
        }

        bitmap = scaleBitmap(bitmap)

        return bitmap
    }

    private fun createColorTextLayout(
        parseText: String,
        visibleText: String,
        codeData: FrameData,
        layoutWidth: Int,
        lineStart: Int
    ): StaticLayout {
        val spannable = getHighLightCode(parseText)
        // если есть выделение текста и оно в видимой части, рисуем его
        if (codeData.selectionStart != codeData.selectionEnd) {
            var selStart = codeData.selectionStart - lineStart + (parseText.length - visibleText.length)
            var selEnd = codeData.selectionEnd - lineStart + (parseText.length - visibleText.length)
            if (selEnd >= 0) {
                if (selStart < 0) {
                    selStart = 0
                }
                if (selEnd < selStart) {
                    val temp = selEnd
                    selEnd = selStart
                    selStart = temp
                }
                val selection = BackgroundColorSpan(selectionColor)
                spannable.setSpan(selection, selStart, selEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        return StaticLayout(
            spannable.subSequence(parseText.length - visibleText.length, spannable.length),
            textCodePaint,
            layoutWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0f,
            false
        )
    }

    private fun getSafeLayoutLine(layout: Layout, line: Int): Int {
        if (line < 0) return 0
        return min(line, layout.lineCount - 1)
    }

    private fun getHighLightCode(code: String): Spannable {
        val spannedString = SpannableString(code)
        // There can be Spans in the future
        return spannedString
    }

    companion object {
        fun scaleBitmap(bitmap: Bitmap): Bitmap {
            // scale bitmap size to video size
            val width = bitmap.width
            val height = bitmap.height

            val widthRatio = VideoRecorder.CODE_VIDEO_WIDTH.toFloat() / width.toFloat()
            val heightRatio = VideoRecorder.CODE_VIDEO_HEIGHT.toFloat() / height.toFloat()
            var finalWidth = floor((width * widthRatio).toDouble()).toInt()
            var finalHeight = floor((height * widthRatio).toDouble()).toInt()
            if (finalWidth > VideoRecorder.CODE_VIDEO_WIDTH || finalHeight > VideoRecorder.CODE_VIDEO_HEIGHT) {
                finalWidth = floor((width * heightRatio).toDouble()).toInt()
                finalHeight = floor((height * heightRatio).toDouble()).toInt()
            }
            return bitmap.scale(finalWidth, finalHeight)
        }
    }
}
