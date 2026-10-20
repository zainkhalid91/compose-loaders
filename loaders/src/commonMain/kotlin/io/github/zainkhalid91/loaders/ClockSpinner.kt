package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 900,
) {
    // durationMillis is one turn of the minute hand; the loop covers a full turn of the hour hand.
    val progress by rememberLoopProgress(durationMillis * 12)
    Canvas(modifier.loader(size)) {
        val r = this.size.minDimension / 2f
        val stroke = r * 0.1f
        drawCircle(color, r - stroke / 2f, style = Stroke(stroke))
        for (i in 0 until 4) {
            rotate(i * 90f) {
                drawCircle(color, stroke * 0.45f, Offset(center.x, center.y - r * 0.7f))
            }
        }
        rotate(progress * 360f) {
            drawLine(color, center, Offset(center.x, center.y - r * 0.38f), stroke, StrokeCap.Round)
        }
        rotate(progress * 360f * 12f) {
            drawLine(color, center, Offset(center.x, center.y - r * 0.58f), stroke * 0.7f, StrokeCap.Round)
        }
        drawCircle(color, stroke * 0.9f)
    }
}
