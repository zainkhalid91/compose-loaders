package io.github.zainkhalid91.loaders

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

// One heartbeat on an ECG trace, as (x, y) fractions of the bounds.
private val Beat = floatArrayOf(
    0f, 0.5f, 0.24f, 0.5f, 0.29f, 0.4f, 0.34f, 0.5f, 0.4f, 0.5f, 0.43f, 0.6f,
    0.48f, 0.06f, 0.53f, 0.94f, 0.57f, 0.5f, 0.65f, 0.5f, 0.72f, 0.36f, 0.79f, 0.5f, 1f, 0.5f,
)

/**
 * A bright pulse running along an ECG trace and fading behind itself.
 * The loader is twice as wide as it is tall; [size] sets the width.
 */
@Composable
public fun HeartbeatLine(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size * 2,
    durationMillis: Int = 1600,
) {
    val progress by rememberLoopProgress(durationMillis)
    Spacer(
        modifier.loader(size, size / 2).drawWithCache {
            val stroke = this.size.height * 0.07f
            val path = Path()
            for (i in Beat.indices step 2) {
                val x = stroke + Beat[i] * (this.size.width - stroke * 2f)
                val y = stroke + Beat[i + 1] * (this.size.height - stroke * 2f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val measure = PathMeasure().apply { setPath(path, false) }
            val length = measure.length
            val trail = length * 0.45f
            val segment = Path()
            val style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            onDrawBehind {
                drawPath(path, color.copy(alpha = color.alpha * 0.12f), style = Stroke(stroke * 0.6f))
                // The head runs past the end so the tail can leave before the loop restarts.
                val head = progress * (length + trail)
                val start = (head - trail).coerceAtLeast(0f)
                val end = head.coerceAtMost(length)
                if (end - start > 1f) {
                    segment.reset()
                    measure.getSegment(start, end, segment, true)
                    val brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, color),
                        startX = measure.getPosition(start).x,
                        endX = measure.getPosition(end).x,
                    )
                    drawPath(segment, brush, style = style)
                }
                if (head <= length) {
                    val tip = measure.getPosition(head)
                    drawCircle(color.copy(alpha = color.alpha * 0.25f), stroke * 1.6f, tip)
                    drawCircle(color, stroke * 0.8f, tip)
                }
            }
        },
    )
}
