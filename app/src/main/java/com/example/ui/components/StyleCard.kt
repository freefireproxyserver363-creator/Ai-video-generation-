package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VisualStyle
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleCard(
    style: VisualStyle,
    isPrimary: Boolean,
    isSecondary: Boolean,
    onSelectPrimary: () -> Unit,
    onSelectSecondary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = isPrimary || isSecondary
    val stylePrimaryColor = Color(style.primaryColor)
    val styleSecondaryColor = Color(style.secondaryColor)

    val borderColor by animateColorAsState(
        targetValue = when {
            isPrimary -> StudioGold
            isSecondary -> StudioViolet
            else -> Color(0xFF262137)
        },
        animationSpec = tween(250),
        label = "styleBorder"
    )

    Surface(
        modifier = modifier
            .testTag("style_card_${style.id}")
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelectPrimary() },
        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon, Title, Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gradient Icon container
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(stylePrimaryColor, styleSecondaryColor)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getStyleIcon(style),
                        contentDescription = style.displayName,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = style.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = style.subtitle,
                        fontSize = 12.sp,
                        color = StudioTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Selection badges
                if (isPrimary) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(StudioGold)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PRIMARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                } else if (isSecondary) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(StudioViolet)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+ BLEND",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = style.description,
                fontSize = 12.sp,
                color = StudioTextSecondary,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Key features tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                style.keyFeatures.take(3).forEach { feature ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1A2C))
                            .border(0.5.dp, Color(0xFF352F4E), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = feature,
                            fontSize = 10.sp,
                            color = StudioTextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Primary select & Secondary toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPrimary) StudioGold.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isPrimary) StudioGold else Color(0xFF383152), RoundedCornerShape(8.dp))
                        .clickable { onSelectPrimary() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isPrimary) "Selected" else "Set Primary",
                        fontSize = 11.sp,
                        fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
                        color = if (isPrimary) StudioGold else StudioTextSecondary
                    )
                }

                if (!isPrimary) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSecondary) StudioViolet.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isSecondary) StudioViolet else Color(0xFF383152), RoundedCornerShape(8.dp))
                            .clickable { onSelectSecondary() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isSecondary) "Remove Blend" else "+ Blend Style",
                            fontSize = 11.sp,
                            fontWeight = if (isSecondary) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSecondary) StudioViolet else StudioTextSecondary
                        )
                    }
                }
            }
        }
    }
}

fun getStyleIcon(style: VisualStyle): ImageVector {
    return when (style) {
        VisualStyle.REALISTIC -> Icons.Default.Videocam
        VisualStyle.ANIME -> Icons.Default.AutoAwesome
        VisualStyle.CARTOON -> Icons.Default.Palette
        VisualStyle.HORROR -> Icons.Default.Nightlight
        VisualStyle.CINEMATIC -> Icons.Default.Movie
        VisualStyle.DRAWING -> Icons.Default.Edit
        VisualStyle.WATERCOLOR -> Icons.Default.Brush
        VisualStyle.COMIC_MANGA -> Icons.Default.MenuBook
        VisualStyle.THREE_D_ANIMATION -> Icons.Default.ViewInAr
        VisualStyle.FANTASY -> Icons.Default.Flare
        VisualStyle.SCI_FI -> Icons.Default.RocketLaunch
        VisualStyle.CLAY_STOP_MOTION -> Icons.Default.Interests
        VisualStyle.PIXEL_ART -> Icons.Default.GridView
        VisualStyle.DARK_FANTASY -> Icons.Default.Shield
        VisualStyle.CUSTOM_STYLE -> Icons.Default.Tune
    }
}
