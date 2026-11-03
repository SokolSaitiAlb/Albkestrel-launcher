package com.albkestrel.launcher.core.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.WorkspaceFolder

@Composable
fun FolderIcon(
    folder: WorkspaceFolder,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    labelColor: Color = Color.White,
    animationSpeed: AnimationSpeed = AnimationSpeed.FLUID_SPRING,
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
        label = "folder_press_scale"
    )

    val cornerRadius = iconSize * 0.28f
    val folderShape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .testTag("folder_icon_${folder.id}")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = 6.dp,
                    shape = folderShape,
                    spotColor = Color(folder.accentColorArgb).copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(folderShape)
                .background(Color.White.copy(alpha = 0.18f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    ),
                    shape = folderShape
                )
                .padding(iconSize * 0.14f),
            contentAlignment = Alignment.Center
        ) {
            // Miniature 2x2 grid representing the first 4 apps
            val previewApps = folder.items.take(4)
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    MiniAppPreview(app = previewApps.getOrNull(0), size = iconSize * 0.32f)
                    MiniAppPreview(app = previewApps.getOrNull(1), size = iconSize * 0.32f)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    MiniAppPreview(app = previewApps.getOrNull(2), size = iconSize * 0.32f)
                    MiniAppPreview(app = previewApps.getOrNull(3), size = iconSize * 0.32f)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = folder.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.5.sp
            ),
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MiniAppPreview(app: LauncherApp?, size: Dp) {
    val miniShape = RoundedCornerShape(size * 0.28f)
    if (app != null) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(miniShape)
                .background(
                    Brush.linearGradient(
                        colors = if (app.gradientColors.size > 1) app.gradientColors else listOf(Color(0xFF334155), Color(0xFF1E293B))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = resolveAppIcon(app.iconSymbol),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(miniShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
    }
}
