package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GenerationState
import com.example.ui.components.CharacterBibleCard
import com.example.ui.components.ContinuityReportCard
import com.example.ui.components.EnvironmentBibleCard
import com.example.ui.components.PipelineFeedbackComponent
import com.example.ui.components.PipelineStepper
import com.example.ui.components.ShotPromptCard
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun PipelineProgressScreen(
    viewModel: StudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val genState by viewModel.generationState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentStageIndex = when (val s = genState) {
        is GenerationState.InProgress -> s.stageIndex
        is GenerationState.Completed -> 13
        else -> 1
    }

    val progressPercent = when (val s = genState) {
        is GenerationState.InProgress -> s.progressPercent
        is GenerationState.Completed -> 100
        else -> 0
    }

    val detailsText = when (val s = genState) {
        is GenerationState.InProgress -> s.details
        is GenerationState.Completed -> "Full movie successfully rendered and assembled!"
        is GenerationState.Error -> "Generation error: ${s.message}"
        else -> "Initializing AI filmmaking pipeline..."
    }

    val plan = (genState as? GenerationState.InProgress)?.currentPlan

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = StudioTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "LIVE PRODUCTION PIPELINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGold
                    )
                    Text(
                        text = plan?.title ?: "Multi-Style AI Generation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }
            }
        }

        // Multi-Step UI Feedback Component (Planning, Generating Frames, Assembling Audio, Finalizing)
        item {
            PipelineFeedbackComponent(
                generationState = genState,
                onViewTheater = onNavigateToPlayer
            )
        }

        // Detailed 13-stage Stepper Component
        item {
            PipelineStepper(
                currentStageIndex = currentStageIndex,
                progressPercent = progressPercent,
                detailsText = detailsText
            )
        }

        // Completion Action
        if (genState is GenerationState.Completed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132219)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioSuccess)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎬 MOVIE READY FOR PREMIERE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioSuccess
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Real frame-to-frame animated AI movie rendered to standard MP4 with multi-style blending and soundscape.",
                            fontSize = 12.sp,
                            color = StudioTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToPlayer,
                            modifier = Modifier
                                .testTag("watch_movie_button")
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSuccess)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WATCH IN THEATER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Production Assets Tabs: Shots, Character Bible, Environment Bible, Continuity
        if (plan != null) {
            item {
                val tabs = listOf("Shots (${plan.scenes.size})", "Character Bible", "Environment Bible", "Continuity Audit")
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = StudioSurface,
                    contentColor = StudioGold,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = StudioGold
                        )
                    }
                ) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == idx) StudioGold else StudioTextSecondary
                                )
                            }
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    items(plan.scenes) { scene ->
                        ShotPromptCard(scene = scene)
                    }
                }
                1 -> {
                    items(plan.characters) { char ->
                        CharacterBibleCard(character = char)
                    }
                }
                2 -> {
                    items(plan.environments) { env ->
                        EnvironmentBibleCard(environment = env)
                    }
                }
                3 -> {
                    item {
                        ContinuityReportCard(report = plan.continuityReport)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
