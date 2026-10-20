package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

/**
 * A single arc whose head races ahead and whose tail catches up, while the
 * whole ring keeps turning. Each [durationMillis] is one grow-and-shrink.
 */
@Composable
public fun ExpressiveArc(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    trackColor: Color = Color.Transparent,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1333,
) {
    // Four grow/shrink cycles advance the start by 4 * 270 = 3 full turns, so the loop is seamless.
    val progress by rememberLoopProgress(durationMillis * 4)
    Canvas(modifier.loader(size)) {
        val d = this.size.minDimension
        val stroke = d * 0.1f
        val cycles = progress * 4f
        val k = floor(cycles)
        val f = cycles - k
        val head = FastOutSlowInEasing.transform((f * 2f).coerceAtMost(1f)) * 270f
        val tail = FastOutSlowInEasing.transform(((f - 0.5f) * 2f).coerceIn(0f, 1f)) * 270f
        val start = k * 270f + tail + progress * 360f * 3f - 90f
        val topLeft = Offset(center.x - d / 2f + stroke / 2f, center.y - d / 2f + stroke / 2f)
        val arcSize = Size(d - stroke, d - stroke)
        if (trackColor.alpha > 0f) {
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
        }
        drawArc(
            color = color,
            startAngle = start,
            sweepAngle = head - tail + 12f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}
