package arg.adegtiarev.videomessage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arg.adegtiarev.videomessage.recorder.VideoRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseVideoViewModel(protected val videoRecorder: VideoRecorder) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private var lastToggleTime = 0L

    fun onToggleRecording() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToggleTime < 1000) return // Debounce
        lastToggleTime = currentTime

        viewModelScope.launch {
            val newState = !_isRecording.value
            _isRecording.value = newState

            if (newState) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }

    protected abstract fun startRecording()

    protected abstract fun stopRecording()

    override fun onCleared() {
        if (_isRecording.value) {
            videoRecorder.stop()
        }
        super.onCleared()
    }
}