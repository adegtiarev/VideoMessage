package arg.adegtiarev.liveletters.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.view.Surface
import java.io.File
import java.io.FileOutputStream

class VideoRecorder(val context: Context, val backgroundColor: Int, private val textBitmapMaker: TextBitmapMaker) {
    private var storyRecorder: MediaRecorder? = null
    private var storySurface: Surface? = null

    private val storyFile: File = File(context.filesDir, CODE_FILE_NAME)
    private val coverFile: File = File(context.filesDir, COVER_IMAGE)

    private var isRecord = false
    private var thread: Thread? = null
    private var codeData: FrameData? = null
    private var lastFrameTime: Long = 0
    private var frameBitmap: Bitmap? = null

    private val runnable = Runnable {
        while (isRecord) {
            if (canAddFrame()) {
                if (codeData != null) {
                    val copy = codeData!!.copy()
                    codeData = null
                    frameBitmap = textBitmapMaker.createFrame(copy)
                }
                if (frameBitmap != null) {
                    storySurface?.let {
                        val canvas = it.lockCanvas(null)
                        val left = (CODE_VIDEO_WIDTH - frameBitmap!!.width) / 2
                        val top = (CODE_VIDEO_HEIGHT - frameBitmap!!.height) / 2
                        canvas.drawColor(backgroundColor)
                        canvas.drawBitmap(frameBitmap!!, left.toFloat(), top.toFloat(), null)
                        it.unlockCanvasAndPost(canvas)
                        lastFrameTime = System.currentTimeMillis()

                    }
                }
            }

        }
    }

    fun start() {
        if (storyFile.exists()) storyFile.delete()
        if (coverFile.exists()) coverFile.delete()

        storyRecorder = MediaRecorder().apply {
            reset()

            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setVideoFrameRate(FRAME_RATE)
            setVideoSize(CODE_VIDEO_WIDTH, CODE_VIDEO_HEIGHT)
            setVideoEncodingBitRate(CODE_VIDEO_WIDTH * CODE_VIDEO_HEIGHT * 5)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)

            setOutputFile(storyFile.absolutePath)
            prepare()
            storySurface = surface
            start()
        }

        thread = Thread(runnable).apply {
            isRecord = true
            start()
        }
    }

    fun stop() {
        thread?.let {
            isRecord = false
            it.join()
        }

        storyRecorder?.apply {
            stop()
            reset()
            release()
        }
        storyRecorder = null

        createCoverImage()
    }


    @Synchronized
    fun addFrame(codeData: FrameData) {
        this.codeData = codeData
    }

    fun addOutputBitmap(bitmap: Bitmap) {
        if (isRecord) {
            frameBitmap = TextBitmapMaker.scaleBitmap(bitmap)
        }
    }

    private fun canAddFrame(): Boolean {
        return System.currentTimeMillis() - lastFrameTime >= FRAME_DELAY_TIME
    }

    private fun createCoverImage() {
        if (frameBitmap == null) {
            throw NullPointerException("Bitmap for story cover is null")
        }
        frameBitmap?.let {
            val coverWidth = it.width
            val coverHeight = (3 * coverWidth) / 4
            val cover = Bitmap.createBitmap(it, 0, 0, coverWidth, coverHeight)
            val out = FileOutputStream(coverFile)
            cover.compress(COVER_FORMAT, COVER_QUALITY, out)
            out.close()
        }
    }

    companion object {

        const val CODE_VIDEO_WIDTH = 960
        const val CODE_VIDEO_HEIGHT = 1280

        const val FRAME_RATE = 20
        const val FRAME_DELAY_TIME: Int = 1000 / FRAME_RATE

        const val CODE_FILE_NAME = "story.mp4"
        const val COVER_IMAGE = "cover.jpg"

        val COVER_FORMAT = Bitmap.CompressFormat.JPEG
        const val COVER_QUALITY = 70
    }
}