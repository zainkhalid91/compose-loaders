package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
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
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 900,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config) { motion ->
        val d = this.size.minDimension
        val stroke = strokePx(strokeWidth, d * 0.1f)
        val radius = (d - stroke * 1.6f) / 2f
        val sweep = 288f
        val brush = Brush.sweepGradient(0f to Color.Transparent, sweep / 360f to color, center = center)
        val style = Stroke(stroke)
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2f, radius * 2f)
        val a = sweep * Pi / 180f
        val head = Offset(center.x + radius * cos(a), center.y + radius * sin(a))
        onDrawBehind {
            rotate(motion.progress * 360f) {
                drawArc(brush, 0f, sweep, false, topLeft, arcSize, style = style)
                drawCircle(color, stroke * 0.8f, head)
            }
        }
    }
}
