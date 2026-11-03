package com.albkestrel.launcher.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albkestrel.launcher.core.designsystem.LivingAppIcon
import com.albkestrel.launcher.core.designsystem.LuminousSurface
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.WorkspaceItem

@Composable
fun LuminousDock(
    dockItems: List<WorkspaceItem>,
    iconSize: Dp,
    showLabels: Boolean,
    animationSpeed: AnimationSpeed,
    modifier: Modifier = Modifier,
    onAppClick: (LauncherApp) -> Unit,
    onOpenDrawer: () -> Unit,
    onAppLongPress: (LauncherApp, Offset) -> Unit = { _, _ -> }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("luminous_dock"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle living drawer handle pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenDrawer)
                .background(Color.White.copy(alpha = 0.14f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = "Swipe up for apps",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "SWIPE UP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.sp
                ),
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Frosted Dock Shelf
        LuminousSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(32.dp),
            backgroundColor = Color.Black.copy(alpha = 0.32f),
            borderColor = Color.White.copy(alpha = 0.22f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockItems.forEach { item ->
                    item.app?.let { app ->
                        LivingAppIcon(
                            app = app,
                            iconSize = iconSize,
                            showLabel = showLabels,
                            labelColor = Color.White,
                            animationSpeed = animationSpeed,
                            isDimmed = !item.isInstalled,
                            onClick = { onAppClick(app) },
                            onLongPress = { pressedApp, anchorPosition ->
                                onAppLongPress(pressedApp, anchorPosition)
                            }
                        )
                    }
                }
            }
        }
    }
}
