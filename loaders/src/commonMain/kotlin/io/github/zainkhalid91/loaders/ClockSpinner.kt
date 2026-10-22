package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp

/** A clock face with hands racing round: the hour hand turns once per 12 minute-hand turns. */
@Composable
public fun ClockSpinner(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 900,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    // durationMillis is one turn of the minute hand; the loop covers a full turn of the hour hand.
    LoaderCanvas(modifier, size, size, durationMillis * 12, progress, config, restPose = 0.36f) { motion ->
        val r = this.size.minDimension / 2f
        val stroke = strokePx(strokeWidth, r * 0.1f)
        val face = Stroke(stroke)
        val tick = Offset(center.x, center.y - r * 0.7f)
        val hourTip = Offset(center.x, center.y - r * 0.38f)
        val minuteTip = Offset(center.x, center.y - r * 0.58f)
        onDrawBehind {
            drawCircle(color, r - stroke / 2f, style = face)
            for (i in 0 until 4) {
                rotate(i * 90f) { drawCircle(color, stroke * 0.45f, tick) }
            }
            rotate(motion.progress * 360f) { drawLine(color, center, hourTip, stroke, StrokeCap.Round) }
            rotate(motion.progress * 360f * 12f) { drawLine(color, center, minuteTip, stroke * 0.7f, StrokeCap.Round) }
            drawCircle(color, stroke * 0.9f)
        }
    }
}
