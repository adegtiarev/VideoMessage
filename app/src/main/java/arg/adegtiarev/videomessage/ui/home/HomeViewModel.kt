package arg.adegtiarev.videomessage.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arg.adegtiarev.videomessage.data.local.VideoEntity
import arg.adegtiarev.videomessage.domain.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    videoRepository: VideoRepository
) : ViewModel() {

    // Получаем список видео, конвертируем Flow в StateFlow для удобства использования в Compose
    // SharingStarted.WhileSubscribed(5000) держит подписку активной еще 5 секунд после поворота экрана
    val videos: StateFlow<List<VideoEntity>> = videoRepository.getAllVideos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
