package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp

// One heartbeat on an ECG trace, as (x, y) fractions of the bounds.
private val Beat = floatArrayOf(
    0f, 0.5f, 0.24f, 0.5f, 0.29f, 0.4f, 0.34f, 0.5f, 0.4f, 0.5f, 0.43f, 0.6f,
    0.48f, 0.06f, 0.53f, 0.94f, 0.57f, 0.5f, 0.65f, 0.5f, 0.72f, 0.36f, 0.79f, 0.5f, 1f, 0.5f,
)

// The trace is resampled at even distances once per size, so a frame only walks an array.
private const val Samples = 240

/**
 * A bright pulse running along an ECG trace and fading behind itself.
 * The loader is twice as wide as it is tall; [size] sets the width.
 */
@Composable
public fun HeartbeatLine(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size * 2,
    durationMillis: Int = 1600,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size / 2, durationMillis, progress, config, restPose = 0.5f) { motion ->
        val stroke = strokePx(strokeWidth, this.size.height * 0.07f)
        val path = Path()
        for (i in Beat.indices step 2) {
            val x = stroke + Beat[i] * (this.size.width - stroke * 2f)
            val y = stroke + Beat[i + 1] * (this.size.height - stroke * 2f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val measure = PathMeasure().apply { setPath(path, false) }
        val length = measure.length
        val xs = FloatArray(Samples + 1)
        val ys = FloatArray(Samples + 1)
        for (i in 0..Samples) {
            val at = measure.getPosition(length * i / Samples)
            xs[i] = at.x
            ys[i] = at.y
        }
        val trail = length * 0.45f
        // An ECG trace only moves rightwards, so the tail's fade can be a horizontal gradient.
        // It is built once with a fixed width; each frame shifts the path into its space instead
        // of creating a new brush.
        val fade = trail * this.size.width / length
        val brush = Brush.horizontalGradient(listOf(Color.Transparent, color), startX = 0f, endX = fade)
        val tail = Path()
        val style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val trackStyle = Stroke(stroke * 0.6f)
        onDrawBehind {
            drawPath(path, color.copy(alpha = color.alpha * 0.12f), style = trackStyle)
            // The head runs past the end so the tail can leave before the loop restarts.
            val head = motion.progress * (length + trail)
            val start = (head - trail).coerceAtLeast(0f)
            val end = head.coerceAtMost(length)
            if (end - start > 0.5f) {
                val shift = sampleAt(xs, ys, length, end).x - fade
                tail.rewind()
                trace(tail, xs, ys, length, start, end, shift)
                translate(left = shift) { drawPath(tail, brush, style = style) }
            }
            if (head <= length) {
                val tip = sampleAt(xs, ys, length, head)
                drawCircle(color.copy(alpha = color.alpha * 0.25f), stroke * 1.6f, tip)
                drawCircle(color, stroke * 0.8f, tip)
            }
        }
    }
}

/** Adds the trace between distances [start] and [end] to [into], moved left by [shift]. */
private fun trace(into: Path, xs: FloatArray, ys: FloatArray, length: Float, start: Float, end: Float, shift: Float) {
    val first = sampleAt(xs, ys, length, start)
    into.moveTo(first.x - shift, first.y)
    var i = (start / length * Samples).toInt() + 1
    val last = (end / length * Samples).toInt()
    while (i <= last) {
        into.lineTo(xs[i] - shift, ys[i])
        i++
    }
    val tip = sampleAt(xs, ys, length, end)
    into.lineTo(tip.x - shift, tip.y)
}

private fun sampleAt(xs: FloatArray, ys: FloatArray, length: Float, distance: Float): Offset {
    val t = (distance / length * Samples).coerceIn(0f, Samples.toFloat())
    val i = t.toInt().coerceAtMost(Samples - 1)
    val f = t - i
    return Offset(lerp(xs[i], xs[i + 1], f), lerp(ys[i], ys[i + 1], f))
}
