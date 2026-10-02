package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.data.model.GenerationMode
import com.example.data.model.InputMode
import com.example.data.model.VisualStyle
import com.example.data.repository.GenerationState
import com.example.ui.components.PipelineFeedbackComponent
import com.example.ui.components.StyleCard
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGoldVariant
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioViolet
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioCreateScreen(
    viewModel: StudioViewModel,
    onNavigateToPipeline: () -> Unit,
    onNavigateToTheater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val genState by viewModel.generationState.collectAsState()
    val prompt by viewModel.prompt.collectAsState()
    val mode by viewModel.selectedMode.collectAsState()
    val inputMode by viewModel.inputMode.collectAsState()
    val primaryStyle by viewModel.primaryStyle.collectAsState()
    val secondaryStyle by viewModel.secondaryStyle.collectAsState()
    val customStyleText by viewModel.customStyleText.collectAsState()
    val styleStrength by viewModel.styleStrength.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val durationMinutes by viewModel.durationMinutes.collectAsState()
    val referenceBitmap by viewModel.referenceBitmap.collectAsState()

    var showAllStyles by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                viewModel.setReferenceBitmap(bmp)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val samplePrompts = listOf(
        "Create a 3-minute horror story about a boy who enters an abandoned school at midnight.",
        "Cyberpunk ronin tracking an AI insurgent through rain-slicked Neo-Tokyo.",
        "A mystical traveler befriending a giant winged forest spirit in ancient ruins.",
        "Space explorer discovering a crystalline cavern pulsing with cosmic energy.",
        "Epic medieval dark fantasy knight facing a shadow colossus on an ash mountain."
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (genState !is GenerationState.Idle) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                PipelineFeedbackComponent(
                    generationState = genState,
                    onViewTheater = onNavigateToTheater,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Title Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(StudioGold, StudioViolet))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Studio",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "MULTI-STYLE AI VIDEO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Cinematic AI Filmmaker Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }
            }
        }

        // 1. Generation Mode Selector
        item {
            Text(
                text = "1. GENERATION MODE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GenerationMode.entries.forEach { m ->
                    val isSelected = mode == m
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) StudioGold else Color(0xFF2C2740),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setMode(m) },
                        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = m.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) StudioGold else StudioTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (m == GenerationMode.STORY_MOVIE) "1-5 min (Primary)" else if (m == GenerationMode.CINEMATIC) "30-60s multi-shot" else "5-10s test",
                                fontSize = 10.sp,
                                color = StudioTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 2. Input Mode Selector
        item {
            Text(
                text = "2. INPUT MODALITY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    InputMode.TEXT_PROMPT to "Text → AI Video",
                    InputMode.IMAGE_REFERENCE to "Image → AI Video",
                    InputMode.STORY_SCRIPT to "Story → Long Movie"
                ).forEach { (im, label) ->
                    val isSelected = inputMode == im
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setInputMode(im) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioGold.copy(alpha = 0.2f),
                            selectedLabelColor = StudioGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Image Reference Upload if selected
            AnimatedVisibility(visible = inputMode == InputMode.IMAGE_REFERENCE) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, StudioViolet, RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    color = StudioSurfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Pick Image",
                            tint = StudioViolet,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (referenceBitmap != null) "Image Reference Loaded ✓" else "Select Reference Image",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "AI will animate real frame-to-frame motion from this visual reference",
                                fontSize = 11.sp,
                                color = StudioTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. User Prompt & Story Script
        item {
            Text(
                text = "3. STORY & CINEMATIC PROMPT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = prompt,
                onValueChange = { viewModel.setPrompt(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_input"),
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioGold,
                    unfocusedBorderColor = Color(0xFF2C2740),
                    focusedContainerColor = StudioSurfaceVariant,
                    unfocusedContainerColor = StudioSurface,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Describe the movie scene, story, and characters...", color = StudioTextTertiary) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sample prompt pills
            Text(text = "Quick Presets:", fontSize = 11.sp, color = StudioTextTertiary)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                samplePrompts.forEach { p ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurfaceVariant)
                            .border(0.5.dp, Color(0xFF383152), RoundedCornerShape(8.dp))
                            .clickable { viewModel.setPrompt(p) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = p.take(38) + "...",
                            fontSize = 10.sp,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        }

        // 4. Style Combination Shortcuts
        item {
            Text(
                text = "4. POPULAR STYLE COMBINATIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VisualStyle.popularCombinations.forEach { (pair, label) ->
                    val isSelected = primaryStyle == pair.first && secondaryStyle == pair.second
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) StudioViolet.copy(alpha = 0.25f) else StudioSurfaceVariant)
                            .border(1.dp, if (isSelected) StudioViolet else Color(0xFF383152), RoundedCornerShape(8.dp))
                            .clickable { viewModel.applyPresetCombination(pair.first, pair.second) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StudioViolet else StudioTextPrimary
                        )
                    }
                }
            }
        }

        // 5. Style Strength Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STYLE STRENGTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioGold
                        )
                        Text(
                            text = "$styleStrength% — " + when {
                                styleStrength <= 10 -> "Minimal"
                                styleStrength <= 35 -> "Subtle"
                                styleStrength <= 60 -> "Balanced"
                                styleStrength <= 85 -> "Strong"
                                else -> "Maximum"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = styleStrength.toFloat(),
                        onValueChange = { viewModel.setStyleStrength(it.toInt()) },
                        valueRange = 0f..100f,
                        steps = 3, // Snap to 0, 25, 50, 75, 100
                        colors = SliderDefaults.colors(
                            thumbColor = StudioGold,
                            activeTrackColor = StudioGold,
                            inactiveTrackColor = Color(0xFF383152)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("0%", "25%", "50%", "75%", "100%").forEach { step ->
                            Text(text = step, fontSize = 10.sp, color = StudioTextTertiary)
                        }
                    }
                }
            }
        }

        // 6. Custom Style Input (if Custom Style selected)
        if (primaryStyle == VisualStyle.CUSTOM_STYLE || secondaryStyle == VisualStyle.CUSTOM_STYLE) {
            item {
                Text(
                    text = "CUSTOM ARTISTIC DIRECTIVE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioViolet
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customStyleText,
                    onValueChange = { viewModel.setCustomStyleText(it) },
                    placeholder = { Text("e.g., Dark gothic anime with hand-painted backgrounds and cinematic lighting", color = StudioTextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioViolet,
                        unfocusedBorderColor = Color(0xFF2C2740),
                        focusedContainerColor = StudioSurfaceVariant,
                        unfocusedContainerColor = StudioSurface,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 7. Visual Style Engine (All 15 Styles)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "5. VISUAL STYLE ENGINE (${VisualStyle.entries.size} STYLES)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (showAllStyles) "Show Less" else "Show All 15",
                    fontSize = 12.sp,
                    color = StudioGold,
                    modifier = Modifier.clickable { showAllStyles = !showAllStyles }
                )
            }
        }

        val displayedStyles = if (showAllStyles) VisualStyle.entries else VisualStyle.entries.take(6)
        items(displayedStyles) { style ->
            StyleCard(
                style = style,
                isPrimary = primaryStyle == style,
                isSecondary = secondaryStyle == style,
                onSelectPrimary = { viewModel.setPrimaryStyle(style) },
                onSelectSecondary = {
                    if (secondaryStyle == style) {
                        viewModel.setSecondaryStyle(null)
                    } else {
                        viewModel.setSecondaryStyle(style)
                    }
                }
            )
        }

        // 8. Duration & Aspect Ratio Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DURATION & ASPECT RATIO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Target Duration:", fontSize = 11.sp, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0.25f to "15s",
                            0.5f to "30s",
                            1.0f to "1m",
                            2.0f to "2m",
                            3.0f to "3m",
                            5.0f to "5m"
                        ).forEach { (dur, label) ->
                            val isSel = durationMinutes == dur
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) StudioGold else StudioSurface)
                                    .clickable { viewModel.setDurationMinutes(dur) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else StudioTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Aspect Ratio:", fontSize = 11.sp, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AspectRatio.entries.forEach { ar ->
                            val isSel = aspectRatio == ar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) StudioViolet else StudioSurface)
                                    .clickable { viewModel.setAspectRatio(ar) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ar.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9. Generate Action Button
        item {
            Button(
                onClick = {
                    viewModel.startGeneration(onStarted = onNavigateToPipeline)
                },
                modifier = Modifier
                    .testTag("generate_video_button")
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioGold
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mode == GenerationMode.STORY_MOVIE) "GENERATE LONG AI MOVIE (${durationMinutes.toInt()} MIN)" else "GENERATE AI VIDEO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
