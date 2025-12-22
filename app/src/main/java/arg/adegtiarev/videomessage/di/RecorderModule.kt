package arg.adegtiarev.videomessage.di

import arg.adegtiarev.videomessage.recorder.VideoRecorder
import arg.adegtiarev.videomessage.recorder.VideoRecorderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class RecorderModule {

    @Binds
    @ViewModelScoped
    abstract fun bindVideoRecorder(impl: VideoRecorderImpl): VideoRecorder
}
