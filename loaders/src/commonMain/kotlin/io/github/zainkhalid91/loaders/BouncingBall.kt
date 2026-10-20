package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** A ball dropping under gravity, squashing on impact, with a shadow that grows as it lands. */
@Composable
public fun BouncingBall(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    shadowColor: Color = color.copy(alpha = color.alpha * 0.45f),
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 900,
) {
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val d = this.size.minDimension
        val radius = d * 0.13f
        val ground = center.y + d * 0.4f
        val jump = d * 0.55f
        // A parabola in time is how height really changes under constant gravity.
        val height = 1f - (2f * progress - 1f).let { it * it }
        val squash = 0.35f * (1f - smoothstep(0f, 0.15f, height))
        val sx = 1f + squash
        val sy = 1f - squash
        val shadowWidth = radius * 2f * (1.2f - 0.6f * height)
        val shadowHeight = radius * 0.35f
        drawOval(
            color = shadowColor.copy(alpha = shadowColor.alpha * (1f - 0.6f * height)),
            topLeft = Offset(center.x - shadowWidth / 2f, ground - shadowHeight / 2f),
            size = Size(shadowWidth, shadowHeight),
        )
        val bottom = ground - height * jump
        drawOval(
            color = color,
            topLeft = Offset(center.x - radius * sx, bottom - radius * 2f * sy),
            size = Size(radius * 2f * sx, radius * 2f * sy),
        )
    }
}
