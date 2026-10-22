package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Two strands of beads twisting around each other, with depth shown by size and fade.
 * The loader is wider than it is tall; [size] sets the width.
 */
@Composable
public fun DnaHelix(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.6f),
    size: Dp = LoaderDefaults.size * 2,
    pairs: Int = 12,
    durationMillis: Int = 2000,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(pairs >= 2) { "pairs must be at least 2, was $pairs" }
    LoaderCanvas(modifier, size, size * 0.5f, durationMillis, progress, config) { motion ->
        val step = this.size.width / pairs
        val beadRadius = step * 0.3f
        val amplitude = this.size.height / 2f - beadRadius
        onDrawBehind {
            for (i in 0 until pairs) {
                // Most of one twist across the width reads as a helix rather than noise.
                val phase = TwoPi * (motion.progress + 0.8f * i / pairs)
                val x = step * (i + 0.5f)
                val top = Offset(x, center.y + amplitude * sin(phase))
                val bottom = Offset(x, center.y - amplitude * sin(phase))
                val depth = cos(phase)
                drawLine(color.copy(alpha = color.alpha * 0.12f), top, bottom, strokeWidth = beadRadius * 0.3f)
                // The strand nearer the viewer is drawn last, on top.
                if (depth >= 0f) {
                    bead(secondaryColor, bottom, beadRadius, -depth)
                    bead(color, top, beadRadius, depth)
                } else {
                    bead(color, top, beadRadius, depth)
                    bead(secondaryColor, bottom, beadRadius, -depth)
                }
            }
        }
    }
}

private fun DrawScope.bead(color: Color, at: Offset, radius: Float, depth: Float) {
    val near = (depth + 1f) / 2f
    drawCircle(color.copy(alpha = color.alpha * (0.35f + 0.65f * near)), radius * (0.6f + 0.4f * near), at)
}
