package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

/**
 * A single arc whose head races ahead and whose tail catches up, while the whole ring keeps
 * turning. Each [durationMillis] is one grow-and-shrink. Pass [brush] for a gradient arc.
 */
@Composable
public fun ExpressiveArc(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    trackColor: Color = Color.Transparent,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1333,
    strokeWidth: Dp = Dp.Unspecified,
    brush: Brush? = null,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    // Four grow/shrink cycles advance the start by 4 * 270 = 3 full turns, so the loop is seamless.
    LoaderCanvas(modifier, size, size, durationMillis * 4, progress, config, restPose = 0.2f) { motion ->
        val d = this.size.minDimension
        val stroke = strokePx(strokeWidth, d * 0.1f)
        val topLeft = Offset(center.x - d / 2f + stroke / 2f, center.y - d / 2f + stroke / 2f)
        val arcSize = Size(d - stroke, d - stroke)
        val trackStyle = Stroke(stroke)
        val arcStyle = Stroke(stroke, cap = StrokeCap.Round)
        onDrawBehind {
            val p = motion.progress
            val cycles = p * 4f
            val k = floor(cycles)
            val f = cycles - k
            val head = FastOutSlowInEasing.transform((f * 2f).coerceAtMost(1f)) * 270f
            val tail = FastOutSlowInEasing.transform(((f - 0.5f) * 2f).coerceIn(0f, 1f)) * 270f
            val start = k * 270f + tail + p * 360f * 3f - 90f
            if (trackColor.alpha > 0f) drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = trackStyle)
            if (brush != null) {
                drawArc(brush, start, head - tail + 12f, false, topLeft, arcSize, style = arcStyle)
            } else {
                drawArc(color, start, head - tail + 12f, false, topLeft, arcSize, style = arcStyle)
            }
        }
    }
}
