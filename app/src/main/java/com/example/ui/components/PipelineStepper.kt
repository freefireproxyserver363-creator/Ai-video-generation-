package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioViolet

val PIPELINE_STAGES = listOf(
    "USER PROMPT",
    "STYLE SELECTION",
    "STORY ANALYSIS",
    "SCENE PLANNING",
    "CHARACTER BIBLE",
    "ENVIRONMENT BIBLE",
    "SHOT PROMPTS",
    "REAL AI VIDEO GENERATION",
    "CONTINUITY CHECK",
    "MULTIPLE SHOTS",
    "VIDEO ASSEMBLY",
    "AUDIO MIXING",
    "FINAL LONG VIDEO"
)

@Composable
fun PipelineStepper(
    currentStageIndex: Int, // 1 to 13
    progressPercent: Int,
    detailsText: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF2C2740), RoundedCornerShape(16.dp)),
        color = StudioSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI FILMMAKING PIPELINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Stage $currentStageIndex of ${PIPELINE_STAGES.size}: ${PIPELINE_STAGES.getOrElse(currentStageIndex - 1) { "Complete" }}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextPrimary
                    )
                }

                Text(
                    text = "$progressPercent%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StudioGold,
                trackColor = Color(0xFF231F33)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Current detail status
            Text(
                text = detailsText,
                fontSize = 12.sp,
                color = StudioTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 13 Step Nodes Grid / Flow
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PIPELINE_STAGES.chunked(3).forEachIndexed { chunkIndex, rowStages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowStages.forEachIndexed { itemIndex, stageName ->
                            val stageNumber = chunkIndex * 3 + itemIndex + 1
                            val isCompleted = currentStageIndex > stageNumber
                            val isCurrent = currentStageIndex == stageNumber

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isCompleted -> StudioSuccess.copy(alpha = 0.15f)
                                            isCurrent -> StudioGold.copy(alpha = 0.2f)
                                            else -> Color(0xFF191626)
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 0.5.dp,
                                        color = when {
                                            isCompleted -> StudioSuccess
                                            isCurrent -> StudioGold
                                            else -> Color(0xFF262137)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Indicator dot or check
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .scale(if (isCurrent) pulseScale else 1f)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCompleted -> StudioSuccess
                                                    isCurrent -> StudioGold
                                                    else -> Color(0xFF383152)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "$stageNumber. $stageName",
                                        fontSize = 9.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isCompleted -> StudioSuccess
                                            isCurrent -> StudioGold
                                            else -> StudioTextTertiary
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
