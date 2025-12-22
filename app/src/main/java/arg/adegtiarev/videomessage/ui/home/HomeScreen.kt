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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import arg.adegtiarev.videomessage.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTextVideo: () -> Unit,
    onNavigateToDrawingVideo: () -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
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
                        imageVector = Icons.Default.Add,
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
                onVideoClick = onNavigateToPlayer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun VideoList(
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Placeholder for video list
    LazyColumn(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text("No videos created yet", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
