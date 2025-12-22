package arg.adegtiarev.videomessage.ui.drawingvideo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawingVideoViewModel @Inject constructor() : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private var lastToggleTime = 0L

    fun onToggleRecording() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToggleTime < 1000) return // Debounce
        lastToggleTime = currentTime

        viewModelScope.launch {
            _isRecording.value = !_isRecording.value
            if (_isRecording.value) {
                // TODO: Start recording logic
            } else {
                // TODO: Stop recording logic
            }
        }
    }
}
