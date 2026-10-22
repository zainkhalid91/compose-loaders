package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/** Two staggered bursts of sparks that fly out, droop under gravity and fade. */
@Composable
public fun FireworkBurst(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.65f),
    size: Dp = LoaderDefaults.size,
    sparks: Int = 12,
    durationMillis: Int = 1600,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(sparks >= 3) { "sparks must be at least 3, was $sparks" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.3f) { motion ->
        val r = this.size.minDimension / 2f
        onDrawBehind {
            burst(color, motion.progress, 0f, r, sparks)
            burst(secondaryColor, fract(motion.progress + 0.5f), 180f / sparks, r, sparks)
        }
    }
}

private fun DrawScope.burst(color: Color, t: Float, angleOffset: Float, r: Float, sparks: Int) {
    val eased = LinearOutSlowInEasing.transform(t)
    val reach = r * 0.85f * eased
    // Each spark is a streak whose tail shortens as it slows down.
    val streak = r * 0.32f * (1f - eased)
    val drop = r * 0.22f * t * t
    // Fade in briefly instead of popping in at full brightness, so a burst never flashes.
    val tint = color.copy(alpha = color.alpha * smoothstep(0f, 0.12f, t) * (1f - t))
    val width = r * 0.09f
    for (i in 0 until sparks) {
        val a = (i * 360f / sparks + angleOffset) * Pi / 180f
        val dx = cos(a)
        val dy = sin(a)
        val tip = Offset(center.x + reach * dx, center.y + reach * dy + drop)
        val tail = Offset(tip.x - streak * dx, tip.y - streak * dy)
        drawLine(tint, tail, tip, width, StrokeCap.Round)
    }
}
