package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt
import kotlin.math.sin

// Ring thickness is picked from this many precomputed strokes, so no Stroke is created per frame.
private const val StrokeSteps = 24

/** Rings that leave a beating core and fade as they spread, like sonar. */
@Composable
public fun PulseRipple(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    rings: Int = 3,
    durationMillis: Int = 1800,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(rings >= 1) { "rings must be at least 1, was $rings" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config) { motion ->
        val maxRadius = this.size.minDimension / 2f
        val thickest = maxRadius * 0.08f
        val strokes = Array(StrokeSteps + 1) { Stroke(1f + thickest * it / StrokeSteps) }
        onDrawBehind {
            for (i in 0 until rings) {
                val phase = fract(motion.progress + i / rings.toFloat())
                val eased = LinearOutSlowInEasing.transform(phase)
                // Rings thin out as they spread.
                val stroke = strokes[((1f - phase) * StrokeSteps).roundToInt()]
                drawCircle(
                    color = color.copy(alpha = color.alpha * (1f - phase)),
                    radius = maxRadius * 0.2f + (maxRadius * 0.8f - stroke.width / 2f) * eased,
                    style = stroke,
                )
            }
            val beat = 1f + 0.12f * sin(motion.progress * TwoPi * rings)
            drawCircle(color, maxRadius * 0.2f * beat)
        }
    }
}
