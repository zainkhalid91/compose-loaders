package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

/**
 * A ring of separate arc segments that fill one by one round the dial, then empty in the same
 * order, like a charging gauge.
 */
@Composable
public fun SegmentedRing(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    trackColor: Color = color.copy(alpha = color.alpha * 0.18f),
    size: Dp = LoaderDefaults.size,
    segments: Int = 10,
    gapDegrees: Float = 8f,
    roundCaps: Boolean = false,
    durationMillis: Int = 2000,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(segments >= 3) { "segments must be at least 3, was $segments" }
    require(gapDegrees >= 0f && gapDegrees < 360f / segments) { "gapDegrees must leave room for each segment" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.3f) { motion ->
        val d = this.size.minDimension
        val stroke = strokePx(strokeWidth, d * 0.12f)
        val style = Stroke(stroke, cap = if (roundCaps) StrokeCap.Round else StrokeCap.Butt)
        val topLeft = Offset(center.x - d / 2f + stroke / 2f, center.y - d / 2f + stroke / 2f)
        val arcSize = Size(d - stroke, d - stroke)
        val step = 360f / segments
        // Round caps grow past the arc ends, so they need a wider gap.
        val gap = gapDegrees + if (roundCaps) stroke / (d / 2f) * 57.3f else 0f
        val sweep = (step - gap).coerceAtLeast(1f)
        onDrawBehind {
            // First half of the loop fills the dial, second half empties it.
            val fill = motion.progress * 2f
            for (i in 0 until segments) {
                val lit = if (fill < 1f) {
                    (fill * segments - i).coerceIn(0f, 1f)
                } else {
                    1f - ((fill - 1f) * segments - i).coerceIn(0f, 1f)
                }
                val start = -90f + i * step + gap / 2f
                drawArc(trackColor, start, sweep, false, topLeft, arcSize, style = style)
                if (lit > 0f) drawArc(color.copy(alpha = color.alpha * lit), start, sweep, false, topLeft, arcSize, style = style)
            }
        }
    }
}
