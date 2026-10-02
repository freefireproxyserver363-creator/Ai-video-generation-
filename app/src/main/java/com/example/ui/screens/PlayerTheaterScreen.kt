package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.AspectRatio
import com.example.data.model.VisualStyle
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioViolet
import com.example.ui.viewmodel.StudioViewModel
import java.io.File

@Composable
fun PlayerTheaterScreen(
    viewModel: StudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project by viewModel.activeProject.collectAsState()
    val scenes by viewModel.activeScenes.collectAsState()

    if (project == null || project?.videoFilePath.isNullOrBlank()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = StudioGold,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Movie Loaded",
                    fontSize = 16.sp,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateBack) {
                    Text("Return to Studio")
                }
            }
        }
        return
    }

    val currentProject = project!!
    val primaryStyle = VisualStyle.fromId(currentProject.primaryStyleId)
    val secondaryStyle = currentProject.secondaryStyleId?.let { VisualStyle.fromId(it) }

    val aspect = when (currentProject.aspectRatio) {
        "9:16" -> AspectRatio.RATIO_9_16
        "2.39:1" -> AspectRatio.RATIO_2_39_1
        "1:1" -> AspectRatio.RATIO_1_1
        else -> AspectRatio.RATIO_16_9
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = StudioTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CINEMA THEATER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGold,
                        letterSpacing = 1.sp
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val file = File(currentProject.videoFilePath ?: "")
                        if (file.exists()) {
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share AI Movie"))
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = StudioGold
                    )
                }
            }
        }

        // Native Video Player
        item {
            VideoPlayerView(
                videoFilePath = currentProject.videoFilePath!!,
                aspectRatioFloat = aspect.ratioFloat,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Film Title & Metadata Badges
        item {
            Column {
                Text(
                    text = currentProject.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Primary Style Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioGold.copy(alpha = 0.2f))
                            .border(1.dp, StudioGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = primaryStyle.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioGold
                        )
                    }

                    // Secondary Style Badge if blended
                    if (secondaryStyle != null && secondaryStyle != primaryStyle) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioViolet.copy(alpha = 0.2f))
                                .border(1.dp, StudioViolet, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "+ ${secondaryStyle.displayName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioViolet
                            )
                        }
                    }

                    Text(
                        text = "• ${currentProject.styleStrength}% Strength • ${currentProject.durationSeconds}s",
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Prompt: ${currentProject.prompt}",
                    fontSize = 12.sp,
                    color = StudioTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // Scene Chapters Breakdown
        item {
            Text(
                text = "SCENE CHAPTERS (${scenes.size} SHOTS)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        items(scenes) { scene ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF2C2740), RoundedCornerShape(12.dp)),
                color = StudioSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(StudioGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${scene.sceneIndex}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scene.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                        }
                        Text(
                            text = "${scene.durationSeconds}s",
                            fontSize = 11.sp,
                            color = StudioGold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Camera: ${scene.cameraMovement}",
                        fontSize = 11.sp,
                        color = StudioGold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = scene.actionDescription,
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )

                    if (scene.narrativeScript.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Narration: \"${scene.narrativeScript}\"",
                            fontSize = 11.sp,
                            color = StudioViolet
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
