package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size * 2,
    durationMillis: Int = 2000,
) {
    val progress by rememberLoopProgress(durationMillis)
    val track = remember { Path() }
    Canvas(modifier.loader(size, size / 2)) {
        val dot = this.size.height * 0.1f
        val a = this.size.width / 2f - dot
        val scaleY = (this.size.height / 2f - dot) / (a * 0.36f)
        fun point(t: Float): Offset {
            val s = sin(t)
            val denominator = 1f + s * s
            return Offset(center.x + a * cos(t) / denominator, center.y + scaleY * a * s * cos(t) / denominator)
        }
        track.reset()
        for (i in 0..96) {
            val p = point(i / 96f * TwoPi)
            if (i == 0) track.moveTo(p.x, p.y) else track.lineTo(p.x, p.y)
        }
        drawPath(track, color.copy(alpha = color.alpha * 0.15f), style = Stroke(dot * 0.5f))
        val trail = 22
        for (j in trail - 1 downTo 0) {
            val fade = 1f - j / trail.toFloat()
            drawCircle(
                color = color.copy(alpha = color.alpha * fade),
                radius = dot * (0.35f + 0.65f * fade),
                center = point(TwoPi * (progress - j * 0.011f)),
            )
        }
    }
}
