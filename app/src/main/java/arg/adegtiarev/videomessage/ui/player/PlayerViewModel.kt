package arg.adegtiarev.videomessage.ui.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import arg.adegtiarev.videomessage.domain.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoRepository: VideoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoFileName: String = checkNotNull(savedStateHandle["videoPath"])
    private val videoFile = File(context.filesDir, videoFileName)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    // Событие для отправки Intent в UI
    private val _shareIntent = MutableSharedFlow<Intent>()
    val shareIntent = _shareIntent.asSharedFlow()

    val player = ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
        prepare()
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = this@apply.duration
                }
            }
        })
    }

    init {
        viewModelScope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    _currentPosition.value = player.currentPosition
                }
                delay(200)
            }
        }
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        _currentPosition.value = position
    }

    fun deleteVideo() {
        viewModelScope.launch {
            videoRepository.deleteVideo(videoFileName)
        }
    }

    fun onShareVideo() {
        if (!videoFile.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            videoFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        viewModelScope.launch {
            _shareIntent.emit(Intent.createChooser(intent, "Share video"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
