package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.sin

/** Rings that leave a beating core and fade as they spread, like sonar. */
@Composable
public fun PulseRipple(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size,
    rings: Int = 3,
    durationMillis: Int = 1800,
) {
    require(rings >= 1) { "rings must be at least 1, was $rings" }
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val maxRadius = this.size.minDimension / 2f
        for (i in 0 until rings) {
            val phase = fract(progress + i / rings.toFloat())
            val eased = LinearOutSlowInEasing.transform(phase)
            val stroke = maxRadius * 0.08f * (1f - phase) + 1f
            drawCircle(
                color = color.copy(alpha = color.alpha * (1f - phase)),
                radius = maxRadius * 0.2f + (maxRadius * 0.8f - stroke / 2f) * eased,
                style = Stroke(stroke),
            )
        }
        val beat = 1f + 0.12f * sin(progress * TwoPi * rings)
        drawCircle(color, maxRadius * 0.2f * beat)
    }
}
