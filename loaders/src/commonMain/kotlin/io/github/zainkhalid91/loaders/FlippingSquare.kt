package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import kotlin.math.abs
import kotlin.math.cos

/** A tile that flips over its horizontal axis, then over its vertical axis, in 3D. */
@Composable
public fun FlippingSquare(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1200,
) {
    val progress by rememberLoopProgress(durationMillis)
    Box(modifier.loader(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize(0.62f)
                .graphicsLayer {
                    val half = progress * 2f
                    val x = FastOutSlowInEasing.transform(half.coerceAtMost(1f)) * 180f
                    val y = FastOutSlowInEasing.transform((half - 1f).coerceIn(0f, 1f)) * 180f
                    rotationX = x
                    rotationY = y
                    cameraDistance = 8f * density
                    // Darken the tile while it is edge-on so the turn reads as depth.
                    val facing = abs(cos(x * Pi / 180f)) * abs(cos(y * Pi / 180f))
                    alpha = 0.55f + 0.45f * facing
                }
                .background(color, RoundedCornerShape(18)),
        )
    }
}
