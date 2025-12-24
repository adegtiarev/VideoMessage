package arg.adegtiarev.videomessage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VideoType {
    TEXT,
    DRAWING
}

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoName: String,
    val fileName: String, // имя файла, например "VIDEO_...mp4"
    val filePath: String, // полный путь к файлу
    val type: VideoType,
    val createdAt: Long = System.currentTimeMillis()
)
