package arg.adegtiarev.videomessage.domain

import android.content.Context
import arg.adegtiarev.videomessage.data.local.VideoDao
import arg.adegtiarev.videomessage.data.local.VideoEntity
import arg.adegtiarev.videomessage.data.local.VideoType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoDao: VideoDao
) {

    fun createNewVideoFile(prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${prefix}_${timestamp}.mp4"
        return File(context.filesDir, fileName)
    }

    suspend fun saveVideo(file: File, type: VideoType) {
        val videoEntity = VideoEntity(
            videoName = file.nameWithoutExtension,
            fileName = file.name,
            filePath = file.absolutePath,
            type = type
        )
        videoDao.insertVideo(videoEntity)
    }

    suspend fun deleteVideo(fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        videoDao.deleteVideoByFileName(fileName)
    }

    fun getAllVideos(): Flow<List<VideoEntity>> {
        return videoDao.getAllVideos()
    }
}
