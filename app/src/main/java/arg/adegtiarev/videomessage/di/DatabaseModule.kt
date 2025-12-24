package arg.adegtiarev.videomessage.di

import android.content.Context
import androidx.room.Room
import arg.adegtiarev.videomessage.data.local.AppDatabase
import arg.adegtiarev.videomessage.data.local.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "video_message_db"
        ).build()
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }
}
