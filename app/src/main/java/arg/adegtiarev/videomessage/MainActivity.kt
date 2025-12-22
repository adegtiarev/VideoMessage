package arg.adegtiarev.videomessage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import arg.adegtiarev.videomessage.ui.VideoMessageNavigation
import arg.adegtiarev.videomessage.ui.theme.VideoMessageTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoMessageTheme {
                VideoMessageNavigation()
            }
        }
    }
}
