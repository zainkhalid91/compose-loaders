package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.sqrt

/** Sand drains through an hourglass, then the glass flips over and starts again. */
@Composable
public fun Hourglass(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    sandColor: Color = color.copy(alpha = color.alpha * 0.7f),
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 2400,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.35f) { motion ->
        val d = this.size.minDimension
        val stroke = strokePx(strokeWidth, d * 0.06f)
        val halfWidth = d * 0.3f
        val chamber = d * 0.38f
        val neck = d * 0.035f
        val cx = center.x
        val cy = center.y
        val top = cy - chamber
        val bottom = cy + chamber
        val glass = Path().apply {
            moveTo(cx - halfWidth, top)
            lineTo(cx + halfWidth, top)
            lineTo(cx + neck, cy)
            lineTo(cx + halfWidth, bottom)
            lineTo(cx - halfWidth, bottom)
            lineTo(cx - neck, cy)
            close()
        }
        val glassStyle = Stroke(stroke, join = StrokeJoin.Round)
        val upper = Path()
        val lower = Path()
        // Sand sits a stroke inside the glass so the outline stays crisp.
        val inset = stroke
        val span = chamber - inset
        fun widthAt(distanceFromNeck: Float) = neck + (halfWidth - inset - neck) * distanceFromNeck / span
        onDrawBehind {
            val p = motion.progress
            // Drain for three quarters of the loop, then flip; the flipped glass looks like a fresh start.
            val remaining = 1f - (p / 0.75f).coerceAtMost(1f)
            val flip = FastOutSlowInEasing.transform(((p - 0.75f) / 0.25f).coerceIn(0f, 1f)) * 180f
            // A cone's volume grows with the square of its height, so height goes with sqrt(volume).
            val upperHeight = span * sqrt(remaining)
            val lowerEmpty = span * sqrt(remaining)
            rotate(flip) {
                if (remaining > 0f) {
                    val y = cy - upperHeight
                    val w = widthAt(upperHeight)
                    upper.rewind()
                    upper.moveTo(cx - w, y)
                    upper.lineTo(cx + w, y)
                    upper.lineTo(cx + neck, cy)
                    upper.lineTo(cx - neck, cy)
                    upper.close()
                    drawPath(upper, sandColor)
                }
                if (remaining < 1f) {
                    val y = cy + lowerEmpty
                    val w = widthAt(lowerEmpty)
                    lower.rewind()
                    lower.moveTo(cx - w, y)
                    lower.lineTo(cx + w, y)
                    lower.lineTo(cx + halfWidth - inset, bottom - inset)
                    lower.lineTo(cx - halfWidth + inset, bottom - inset)
                    lower.close()
                    drawPath(lower, sandColor)
                }
                if (remaining > 0f && remaining < 1f) {
                    drawLine(sandColor, Offset(cx, cy), Offset(cx, cy + lowerEmpty), stroke * 0.5f, StrokeCap.Round)
                }
                drawPath(glass, color, style = glassStyle)
            }
        }
    }
}
