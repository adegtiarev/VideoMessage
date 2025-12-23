package arg.adegtiarev.videomessage.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import arg.adegtiarev.videomessage.ui.drawingvideo.DrawingVideoScreen
import arg.adegtiarev.videomessage.ui.home.HomeScreen
import arg.adegtiarev.videomessage.ui.player.PlayerScreen
import arg.adegtiarev.videomessage.ui.textvideo.TextVideoScreen

object Destinations {
    const val HOME = "home"
    const val TEXT_VIDEO = "text_video"
    const val DRAWING_VIDEO = "drawing_video"
    const val PLAYER = "player/{videoPath}"
}

@Composable
fun VideoMessageNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {
        composable(Destinations.HOME) {
            HomeScreen(
                onNavigateToTextVideo = { navController.navigate(Destinations.TEXT_VIDEO) },
                onNavigateToDrawingVideo = { navController.navigate(Destinations.DRAWING_VIDEO) },
                onNavigateToPlayer = { path -> navController.navigate("player/$path") }
            )
        }
        composable(Destinations.TEXT_VIDEO) {
            TextVideoScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { path ->
                    navController.navigate("player/$path") {
                        // Убираем экран создания видео из стека, чтобы при нажатии "Назад"
                        // пользователь попадал в Home, а не обратно в экран создания
                        popUpTo(Destinations.HOME) {
                            saveState = false
                        }
                    }
                }
            )
        }
        composable(Destinations.DRAWING_VIDEO) {
            DrawingVideoScreen(onBack = { navController.popBackStack() })
        }
        composable(Destinations.PLAYER) { backStackEntry ->
            val videoPath = backStackEntry.arguments?.getString("videoPath") ?: ""
            PlayerScreen(videoPath = videoPath, onBack = { navController.popBackStack() })
        }
    }
}
