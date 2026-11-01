package com.albkestrel.launcher.core.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.LauncherApp

@Composable
fun LivingAppIcon(
    app: LauncherApp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    labelColor: Color = Color.White,
    animationSpeed: AnimationSpeed = AnimationSpeed.FLUID_SPRING,
    isDimmed: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = when (animationSpeed) {
            AnimationSpeed.FLUID_SPRING -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
            AnimationSpeed.SNAPPY -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
            AnimationSpeed.REDUCED_MOTION -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        },
        label = "icon_press_scale"
    )

    val cornerRadius = iconSize * 0.28f
    val iconShape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .testTag("app_icon_${app.id}")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            // Luminous ambient shadow
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .shadow(
                        elevation = 6.dp,
                        shape = iconShape,
                        spotColor = app.gradientColors.firstOrNull()?.copy(alpha = 0.4f) ?: Color.Transparent,
                        ambientColor = Color.Black.copy(alpha = 0.2f)
                    )
            )

            // Primary squircle background
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(iconShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (app.gradientColors.size > 1) {
                                if (isDimmed) app.gradientColors.map { it.copy(alpha = 0.45f) } else app.gradientColors
                            } else {
                                listOf(Color(0xFF334155), Color(0xFF1E293B))
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = iconShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle top specular highlight arc
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 40f
                            )
                        )
                )

                // Vector glyph
                Icon(
                    imageVector = resolveAppIcon(app.iconSymbol),
                    contentDescription = app.name,
                    tint = if (isDimmed) Color.White.copy(alpha = 0.5f) else Color.White,
                    modifier = Modifier.size(iconSize * 0.52f)
                )
            }

            // Notification Badge
            if (app.badgeCount != null && app.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .border(1.5.dp, Color.White, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (app.badgeCount > 99) "99+" else app.badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isDimmed) labelColor.copy(alpha = 0.5f) else labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun resolveAppIcon(symbol: String): ImageVector {
    return when (symbol.lowercase()) {
        "phone" -> Icons.Rounded.Phone
        "message" -> Icons.Rounded.ChatBubble
        "public" -> Icons.Rounded.Public
        "camera" -> Icons.Rounded.PhotoCamera
        "photo_library" -> Icons.Rounded.PhotoLibrary
        "music_note" -> Icons.Rounded.MusicNote
        "settings" -> Icons.Rounded.Settings
        "schedule" -> Icons.Rounded.Schedule
        "wb_sunny" -> Icons.Rounded.WbSunny
        "calendar_today" -> Icons.Rounded.CalendarToday
        "folder" -> Icons.Rounded.Folder
        "edit_note" -> Icons.Rounded.EditNote
        "favorite" -> Icons.Rounded.Favorite
        "explore" -> Icons.Rounded.Explore
        "auto_awesome" -> Icons.Rounded.AutoAwesome
        "calculate" -> Icons.Rounded.Calculate
        "mic" -> Icons.Rounded.Mic
        "sports_esports" -> Icons.Rounded.SportsEsports
        "android" -> Icons.Rounded.Android
        else -> Icons.Rounded.Apps
    }
}
