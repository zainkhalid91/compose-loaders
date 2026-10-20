package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/** A glowing head pulling a tail that fades to nothing behind it. */
@Composable
public fun CometSpinner(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 900,
) {
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val d = this.size.minDimension
        val stroke = d * 0.1f
        val radius = (d - stroke * 1.6f) / 2f
        val sweep = 288f
        val brush = Brush.sweepGradient(
            0f to Color.Transparent,
            sweep / 360f to color,
            center = center,
        )
        rotate(progress * 360f) {
            drawArc(
                brush = brush,
                startAngle = 0f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(stroke),
            )
            val a = sweep * Pi / 180f
            drawCircle(color, stroke * 0.8f, Offset(center.x + radius * cos(a), center.y + radius * sin(a)))
        }
    }
}
