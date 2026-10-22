package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A dot tracing a figure-eight, with a shrinking trail behind it.
 * The loader is twice as wide as it is tall; [size] sets the width.
 */
@Composable
public fun InfinityTrail(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size * 2,
    durationMillis: Int = 2000,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size / 2, durationMillis, progress, config) { motion ->
        val dot = this.size.height * 0.1f
        val a = this.size.width / 2f - dot
        val scaleY = (this.size.height / 2f - dot) / (a * 0.36f)
        val cx = center.x
        val cy = center.y
        val track = Path()
        for (i in 0..96) {
            val p = lemniscate(i / 96f * TwoPi, cx, cy, a, scaleY)
            if (i == 0) track.moveTo(p.x, p.y) else track.lineTo(p.x, p.y)
        }
        val trackStyle = Stroke(dot * 0.5f)
        onDrawBehind {
            drawPath(track, color.copy(alpha = color.alpha * 0.15f), style = trackStyle)
            val trail = 22
            for (j in trail - 1 downTo 0) {
                val fade = 1f - j / trail.toFloat()
                drawCircle(
                    color = color.copy(alpha = color.alpha * fade),
                    radius = dot * (0.35f + 0.65f * fade),
                    center = lemniscate(TwoPi * (motion.progress - j * 0.011f), cx, cy, a, scaleY),
                )
            }
        }
    }
}

/** Point on a lemniscate of Bernoulli (figure-eight) at parameter [t]. */
private fun lemniscate(t: Float, cx: Float, cy: Float, a: Float, scaleY: Float): Offset {
    val s = sin(t)
    val c = cos(t)
    val denominator = 1f + s * s
    return Offset(cx + a * c / denominator, cy + scaleY * a * s * c / denominator)
}
