package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.PlannedScene
import com.example.data.model.CharacterBibleEntry
import com.example.data.model.ContinuityCheckResult
import com.example.data.model.EnvironmentBibleEntry
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioViolet

@Composable
fun CharacterBibleCard(character: CharacterBibleEntry, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF2C2740), RoundedCornerShape(12.dp)),
        color = StudioSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(StudioViolet.copy(alpha = 0.2f))
                        .border(1.dp, StudioViolet, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = StudioViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = character.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = character.role,
                        fontSize = 11.sp,
                        color = StudioGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Appearance: ${character.visualAppearance}",
                fontSize = 11.sp,
                color = StudioTextSecondary,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Costume: ${character.costume}",
                fontSize = 11.sp,
                color = StudioTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Locked Consistency Tokens: ${character.consistencyTokens}",
                fontSize = 10.sp,
                color = StudioTextTertiary
            )
        }
    }
}

@Composable
fun EnvironmentBibleCard(environment: EnvironmentBibleEntry, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF2C2740), RoundedCornerShape(12.dp)),
        color = StudioSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(StudioGold.copy(alpha = 0.2f))
                        .border(1.dp, StudioGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = environment.locationName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = environment.timeOfDay,
                        fontSize = 11.sp,
                        color = StudioGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Atmosphere: ${environment.atmosphericConditions}",
                fontSize = 11.sp,
                color = StudioTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lighting: ${environment.lightingSetup}",
                fontSize = 11.sp,
                color = StudioTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Palette: ${environment.colorPaletteKeywords}",
                fontSize = 10.sp,
                color = StudioTextTertiary
            )
        }
    }
}

@Composable
fun ContinuityReportCard(report: ContinuityCheckResult, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, StudioSuccess.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        color = Color(0xFF0F1A15),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = StudioSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONTINUITY AUDIT PASSED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioSuccess,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "${report.scorePercent}% Match",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioSuccess
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            report.recommendations.forEach { rec ->
                Text(
                    text = "• $rec",
                    fontSize = 11.sp,
                    color = StudioTextSecondary
                )
            }
        }
    }
}

@Composable
fun ShotPromptCard(scene: PlannedScene, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) StudioGold else Color(0xFF2C2740),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = StudioGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                    fontWeight = FontWeight.Bold,
                    color = StudioGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Camera: ${scene.cameraMovement.label} | ${scene.lightingSetup}",
                fontSize = 11.sp,
                color = StudioGold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = scene.actionDescription,
                fontSize = 11.sp,
                color = StudioTextSecondary,
                lineHeight = 15.sp
            )

            if (scene.narrativeScript.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Voiceover: \"${scene.narrativeScript}\"",
                    fontSize = 11.sp,
                    color = StudioViolet
                )
            }
        }
    }
}
