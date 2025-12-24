package arg.adegtiarev.videomessage.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import arg.adegtiarev.videomessage.R
import arg.adegtiarev.videomessage.data.local.VideoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTextVideo: () -> Unit,
    onNavigateToDrawingVideo: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val videos by viewModel.videos.collectAsState()
    var isFabExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (isFabExpanded) 45f else 0f, label = "fab_rotation")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Video Message") })
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secondary FABs
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + scaleIn() + expandVertically(),
                    exit = fadeOut() + scaleOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onNavigateToDrawingVideo()
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(painterResource(R.drawable.ic_brush), contentDescription = "Draw Video")
                        }
                        
                        FloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onNavigateToTextVideo()
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(painterResource(R.drawable.ic_letter_a), contentDescription = "Text Video")
                        }
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        painterResource(R.drawable.ic_record), // Or some other icon
                        contentDescription = "Create Video",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    ) { paddingValues ->
        // Overlay to dismiss FABs when clicking outside
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
             if (isFabExpanded) {
                 Box(
                     modifier = Modifier
                         .fillMaxSize()
                         .clickable(
                             indication = null,
                             interactionSource = remember { MutableInteractionSource() }
                         ) { isFabExpanded = false }
                 )
             }
            
            VideoList(
                videos = videos,
                onVideoClick = onNavigateToPlayer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun VideoList(
    videos: List<VideoEntity>,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) {
        Box(
            modifier = modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No videos created yet", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(videos, key = { it.id }) {
                VideoItem(video = it, onClick = { onVideoClick(it.fileName) })
            }
        }
    }
}

@Composable
fun VideoItem(video: VideoEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = video.videoName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Type: ${video.type}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Created: ${formatTimestamp(video.createdAt)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(date)
}
