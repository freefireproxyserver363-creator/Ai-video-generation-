package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.VisualStyle
import com.example.ui.components.getStyleIcon
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioViolet
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleLabScreen(
    viewModel: StudioViewModel,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioViolet),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Style Lab",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "STYLE COMPENDIUM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "15 Visual Styles & Combination Lab",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = "Each style features dedicated procedural physics, camera grammar, lighting shaders, and audio synthesis.",
                fontSize = 12.sp,
                color = StudioTextSecondary,
                lineHeight = 16.sp
            )
        }

        items(VisualStyle.entries) { style ->
            StyleLabItemCard(
                style = style,
                onUseStyle = {
                    viewModel.setPrimaryStyle(style)
                    val recipePrompt = when (style) {
                        VisualStyle.REALISTIC -> "Hyperrealistic documentary of deep ocean explorers encountering a colossal bioluminescent creature."
                        VisualStyle.ANIME -> "Create a 3-minute horror story about a boy who enters an abandoned school at midnight."
                        VisualStyle.CARTOON -> "Playful animated chase between an eccentric inventor and their runaway steam-powered toaster."
                        VisualStyle.HORROR -> "Psychological horror sequence of a night guard investigating whispering footsteps in an abandoned hospital wing."
                        VisualStyle.CINEMATIC -> "Hollywood crime thriller tracking shot through a rain-drenched alleyway in 1970s Chicago."
                        VisualStyle.DRAWING -> "Frame-to-frame hand-drawn animated sequence where an architect's blueprint sketches come alive and construct a clockwork city."
                        VisualStyle.WATERCOLOR -> "Poetic watercolor tale of migratory cranes flying across autumnal misty mountain lakes."
                        VisualStyle.COMIC_MANGA -> "High-octane superhero comic battle with dramatic speed paneling and ink impact explosions."
                        VisualStyle.THREE_D_ANIMATION -> "Feature film 3D animation of a tiny clockwork automaton embarking on a journey across a giant library."
                        VisualStyle.FANTASY -> "Mythic quest through floating quartz crystals guarding the celestial dragon's aerie."
                        VisualStyle.SCI_FI -> "Cyberpunk detective pursuing a rogue synth through the vertical hologram highways of Neo-Kyoto."
                        VisualStyle.CLAY_STOP_MOTION -> "Tactile handcrafted stop-motion adventure of a clay baker preparing a magical feast with 12fps cadence."
                        VisualStyle.PIXEL_ART -> "16-bit retro arcade sci-fi rover traversing an alien planet with parallax crystal caverns."
                        VisualStyle.DARK_FANTASY -> "Eldritch knight descending into the sunken obsidian tomb of the forgotten sun king."
                        VisualStyle.CUSTOM_STYLE -> "Create this as a dark gothic anime with hand-painted backgrounds and cinematic lighting."
                    }
                    viewModel.setPrompt(recipePrompt)
                    onNavigateToCreate()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleLabItemCard(
    style: VisualStyle,
    onUseStyle: () -> Unit
) {
    val stylePrimaryColor = Color(style.primaryColor)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF2C2740), RoundedCornerShape(16.dp)),
        color = StudioSurfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(stylePrimaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getStyleIcon(style),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = style.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = style.subtitle,
                        fontSize = 11.sp,
                        color = StudioGold
                    )
                }

                Button(
                    onClick = onUseStyle,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGold.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Apply Preset", fontSize = 11.sp, color = StudioGold, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = style.description,
                fontSize = 12.sp,
                color = StudioTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                style.keyFeatures.forEach { f ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1A2C))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(text = "• $f", fontSize = 10.sp, color = StudioTextTertiary)
                    }
                }
            }
        }
    }
}
