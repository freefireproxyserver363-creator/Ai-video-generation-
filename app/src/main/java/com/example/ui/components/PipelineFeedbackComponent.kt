package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GenerationState
import com.example.data.repository.PipelineStageGroup
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioViolet

/**
 * Multi-Step UI Feedback Component showing real-time pipeline status:
 * 1. Planning
 * 2. Generating Frames
 * 3. Assembling Audio
 * 4. Finalizing Video
 */
@Composable
fun PipelineFeedbackComponent(
    generationState: GenerationState,
    onViewTheater: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (generationState is GenerationState.Idle) return

    var isExpanded by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val currentMacroStage = when (generationState) {
        is GenerationState.InProgress -> generationState.macroStage
        is GenerationState.Completed -> PipelineStageGroup.FINALIZING
        else -> PipelineStageGroup.PLANNING
    }

    val progressPercent = when (generationState) {
        is GenerationState.InProgress -> generationState.progressPercent
        is GenerationState.Completed -> 100
        else -> 0
    }

    val detailsText = when (generationState) {
        is GenerationState.InProgress -> generationState.details
        is GenerationState.Completed -> "Movie mastered in full HD! Ready to stream in Cinema Theater."
        is GenerationState.Error -> "Pipeline error: ${generationState.message}"
        else -> ""
    }

    val activeColor = when (generationState) {
        is GenerationState.Completed -> StudioSuccess
        is GenerationState.Error -> Color(0xFFFF5252)
        else -> StudioGold
    }

    Surface(
        modifier = modifier
            .testTag("pipeline_feedback_component")
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        activeColor,
                        StudioViolet,
                        activeColor
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        color = StudioSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title, Active Stage Name, Percentage, Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(activeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (generationState is GenerationState.InProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = StudioGold,
                                strokeWidth = 2.5.dp
                            )
                        } else if (generationState is GenerationState.Completed) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StudioSuccess,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (generationState is GenerationState.Completed) "GENERATION COMPLETE" else "PIPELINE ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = when (generationState) {
                                is GenerationState.InProgress -> "Stage ${currentMacroStage.stepNumber}/4: ${currentMacroStage.title}"
                                is GenerationState.Completed -> "Movie Premiere Ready"
                                is GenerationState.Error -> "Pipeline Interrupted"
                                else -> ""
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$progressPercent%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor
                    )
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Details",
                            tint = StudioTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = activeColor,
                trackColor = Color(0xFF231F33)
            )

            // Current detail status
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detailsText,
                fontSize = 12.sp,
                color = StudioTextSecondary,
                lineHeight = 16.sp,
                maxLines = if (isExpanded) 3 else 1,
                overflow = TextOverflow.Ellipsis
            )

            // Expanded Multi-Step Feedback Nodes
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // 4 Milestone Macro Steps
                    PipelineStageGroup.entries.forEach { stageGroup ->
                        val isCompleted = when (generationState) {
                            is GenerationState.Completed -> true
                            is GenerationState.InProgress -> stageGroup.stepNumber < currentMacroStage.stepNumber
                            else -> false
                        }

                        val isCurrent = when (generationState) {
                            is GenerationState.InProgress -> stageGroup.stepNumber == currentMacroStage.stepNumber
                            else -> false
                        }

                        val stepBg by animateColorAsState(
                            targetValue = when {
                                isCompleted -> StudioSuccess.copy(alpha = 0.12f)
                                isCurrent -> StudioGold.copy(alpha = 0.18f)
                                else -> Color(0xFF161324)
                            },
                            label = "stepBg"
                        )

                        val stepBorder by animateColorAsState(
                            targetValue = when {
                                isCompleted -> StudioSuccess.copy(alpha = 0.6f)
                                isCurrent -> StudioGold
                                else -> Color(0xFF2A243D)
                            },
                            label = "stepBorder"
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isCurrent) 1.5.dp else 0.5.dp,
                                    color = stepBorder,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = stepBg,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Step indicator circle
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(if (isCurrent) pulseScale else 1f)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCompleted -> StudioSuccess
                                                isCurrent -> StudioGold
                                                else -> Color(0xFF322C47)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${stageGroup.stepNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) Color.Black else StudioTextTertiary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = stageGroup.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isCompleted -> StudioSuccess
                                                isCurrent -> StudioGold
                                                else -> StudioTextTertiary
                                            }
                                        )

                                        if (isCurrent) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(StudioGold)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "IN PROGRESS",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = if (isCurrent && generationState is GenerationState.InProgress && stageGroup == PipelineStageGroup.GENERATING_FRAMES && generationState.totalScenesCount > 0) {
                                            "Rendering Shot ${generationState.renderedScenesCount + 1}/${generationState.totalScenesCount} @ 30 FPS"
                                        } else {
                                            stageGroup.subtitle
                                        },
                                        fontSize = 10.sp,
                                        color = if (isCurrent) StudioTextPrimary else StudioTextTertiary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = getMacroStageIcon(stageGroup),
                                    contentDescription = null,
                                    tint = when {
                                        isCompleted -> StudioSuccess
                                        isCurrent -> StudioGold
                                        else -> StudioTextTertiary
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Completed action CTA button
                    if (generationState is GenerationState.Completed && onViewTheater != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onViewTheater() },
                            color = StudioSuccess,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OPEN IN THEATER",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getMacroStageIcon(stageGroup: PipelineStageGroup): ImageVector {
    return when (stageGroup) {
        PipelineStageGroup.PLANNING -> Icons.Default.Assignment
        PipelineStageGroup.GENERATING_FRAMES -> Icons.Default.MovieCreation
        PipelineStageGroup.ASSEMBLING_AUDIO -> Icons.Default.Audiotrack
        PipelineStageGroup.FINALIZING -> Icons.Default.CheckCircle
    }
}
