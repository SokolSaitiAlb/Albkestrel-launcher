package com.albkestrel.launcher.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LuminousSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    borderColor: Color = Color.White.copy(alpha = 0.22f),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val luminousBrush = Brush.linearGradient(
        colors = listOf(
            borderColor,
            borderColor.copy(alpha = 0.06f),
            borderColor.copy(alpha = 0.16f)
        )
    )

    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(elevation, shape, clip = false)
                } else Modifier
            )
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, luminousBrush, shape),
        content = content
    )
}
