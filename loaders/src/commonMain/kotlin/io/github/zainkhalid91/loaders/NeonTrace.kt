package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

private const val Samples = 200

/**
 * A neon sign drawing itself: a glowing pen traces the outline, the whole sign flickers up to full
 * glow, then the line is wiped away from where it started.
 */
@Composable
public fun NeonTrace(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    glowColor: Color = color.copy(alpha = color.alpha * 0.3f),
    size: Dp = LoaderDefaults.size,
    cornerPercent: Int = 30,
    durationMillis: Int = 2400,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(cornerPercent in 0..50) { "cornerPercent must be 0 to 50, was $cornerPercent" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.4f) { motion ->
        val d = this.size.minDimension
        val stroke = strokePx(strokeWidth, d * 0.07f)
        val inset = stroke * 1.6f
        val side = d - inset * 2f
        val radius = side * cornerPercent / 100f
        val outline = Path().apply {
            addRoundRect(RoundRect(center.x - side / 2f, center.y - side / 2f, center.x + side / 2f, center.y + side / 2f, CornerRadius(radius)))
        }
        // Resampled once so each frame only walks arrays into a reused path.
        val measure = PathMeasure().apply { setPath(outline, false) }
        val length = measure.length
        val xs = FloatArray(Samples + 1)
        val ys = FloatArray(Samples + 1)
        for (i in 0..Samples) {
            val at = measure.getPosition(length * i / Samples)
            xs[i] = at.x
            ys[i] = at.y
        }
        val line = Path()
        val core = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val glow = Stroke(stroke * 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        onDrawBehind {
            val p = motion.progress
            // Trace on for 45%, glow for 20%, wipe off for 35%.
            val head = FastOutSlowInEasing.transform((p / 0.45f).coerceAtMost(1f))
            val tail = FastOutSlowInEasing.transform(((p - 0.65f) / 0.35f).coerceIn(0f, 1f))
            val glowBoost = smoothstep(0.42f, 0.5f, p) * (1f - smoothstep(0.6f, 0.68f, p))
            drawPath(outline, color.copy(alpha = color.alpha * 0.1f), style = core)
            if (head - tail > 0.002f) {
                line.rewind()
                val from = (tail * Samples).toInt()
                val to = (head * Samples).toInt()
                line.moveTo(xs[from], ys[from])
                for (i in from + 1..to) line.lineTo(xs[i], ys[i])
                drawPath(line, glowColor.copy(alpha = glowColor.alpha * (0.6f + 0.4f * glowBoost)), style = glow)
                drawPath(line, color, style = core)
            }
        }
    }
}
