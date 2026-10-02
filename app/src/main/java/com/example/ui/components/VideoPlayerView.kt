package com.example.ui.components

import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun VideoPlayerView(
    videoFilePath: String,
    aspectRatioFloat: Float = 16f / 9f,
    modifier: Modifier = Modifier,
    onSeekToScene: ((Int) -> Unit)? = null
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(1) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    // Safe polling for progress
    LaunchedEffect(isPlaying, isPrepared) {
        while (isPlaying && isPrepared) {
            try {
                videoViewRef?.let { vv ->
                    if (vv.isPlaying) {
                        currentPositionMs = vv.currentPosition
                        val dur = vv.duration
                        if (dur > 0) durationMs = dur
                    }
                }
            } catch (e: Exception) {
                Log.w("VideoPlayerView", "Error querying position: ${e.message}")
            }
            delay(200)
        }
    }

    // Auto-hide controls
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(3500)
            isControlsVisible = false
        }
    }

    val file = remember(videoFilePath) { File(videoFilePath) }
    val isValidFile = file.exists() && file.length() > 0

    Box(
        modifier = modifier
            .testTag("video_player_container")
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
            },
        contentAlignment = Alignment.Center
    ) {
        if (isValidFile && !hasError) {
            // Android native VideoView with crash-proof error listeners
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatioFloat.coerceIn(0.5f, 2.5f)),
                factory = { context ->
                    VideoView(context).apply {
                        videoViewRef = this

                        // CRITICAL: setOnErrorListener prevents "Can't play this video" popup crashes!
                        setOnErrorListener { _, what, extra ->
                            Log.e("VideoPlayerView", "Handled video playback error: what=$what, extra=$extra")
                            hasError = true
                            isPlaying = false
                            true // Return true = handled!
                        }

                        setOnPreparedListener { mp ->
                            try {
                                val dur = mp.duration
                                durationMs = if (dur > 0) dur else 1000
                                mp.isLooping = true
                                isPrepared = true
                                start()
                                isPlaying = true
                            } catch (e: Exception) {
                                Log.e("VideoPlayerView", "Prepared error: ${e.message}")
                            }
                        }

                        setOnCompletionListener {
                            isPlaying = false
                            currentPositionMs = durationMs
                        }

                        try {
                            setVideoURI(Uri.fromFile(file))
                        } catch (e: Exception) {
                            Log.e("VideoPlayerView", "setVideoURI failed: ${e.message}")
                            hasError = true
                        }
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                }
            )
        } else {
            // Safe fallback placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatioFloat.coerceIn(0.5f, 2.5f))
                    .background(Color(0xFF14121E)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (hasError) "Rendering Video Stream..." else "Loading Video...",
                        fontSize = 13.sp,
                        color = StudioTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = StudioGold,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = isControlsVisible && !hasError,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Center Big Play/Pause Button
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                try {
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                } catch (e: Exception) {
                                    Log.w("VideoPlayerView", "Toggle play error: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier
                            .testTag("play_pause_button")
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(StudioGold.copy(alpha = 0.9f))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Bottom Timeline Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Slider
                    Slider(
                        value = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = { frac ->
                            val targetMs = (frac * durationMs).toInt()
                            currentPositionMs = targetMs
                            try {
                                videoViewRef?.seekTo(targetMs)
                            } catch (e: Exception) {
                                // ignore
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = StudioGold,
                            activeTrackColor = StudioGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )

                    // Timecode & Secondary controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatTimecode(currentPositionMs)} / ${formatTimecode(durationMs)}",
                            fontSize = 12.sp,
                            color = StudioTextPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    try {
                                        videoViewRef?.let { vv ->
                                            val target = (vv.currentPosition - 5000).coerceAtLeast(0)
                                            vv.seekTo(target)
                                            currentPositionMs = target
                                        }
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "Rewind 5s",
                                    tint = StudioTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        videoViewRef?.let { vv ->
                                            val target = (vv.currentPosition + 5000).coerceAtMost(durationMs)
                                            vv.seekTo(target)
                                            currentPositionMs = target
                                        }
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Forward 5s",
                                    tint = StudioTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        videoViewRef?.seekTo(0)
                                        videoViewRef?.start()
                                        isPlaying = true
                                        currentPositionMs = 0
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Restart",
                                    tint = StudioTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(videoFilePath) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}

private fun formatTimecode(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
