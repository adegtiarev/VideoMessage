package arg.adegtiarev.videomessage.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createNewVideoFile(prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${prefix}_${timestamp}.mp4"
        return File(context.filesDir, fileName)
    }
}
