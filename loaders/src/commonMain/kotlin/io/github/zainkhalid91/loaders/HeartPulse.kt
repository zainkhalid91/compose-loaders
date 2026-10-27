package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import kotlin.math.sin

/**
 * A heart beating with a real "lub-dub" double beat, sending out a fading outline echo on the
 * first beat.
 */
@Composable
public fun HeartPulse(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    showEcho: Boolean = true,
    durationMillis: Int = 1200,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.05f) { motion ->
        val d = this.size.minDimension
        val w = d * 0.62f
        val h = d * 0.56f
        val left = center.x - w / 2f
        val top = center.y - h / 2f + d * 0.02f
        val heart = Path().apply {
            moveTo(center.x, top + h * 0.28f)
            cubicTo(center.x, top, left, top, left, top + h * 0.32f)
            cubicTo(left, top + h * 0.62f, center.x - w * 0.12f, top + h * 0.8f, center.x, top + h)
            cubicTo(center.x + w * 0.12f, top + h * 0.8f, left + w, top + h * 0.62f, left + w, top + h * 0.32f)
            cubicTo(left + w, top, center.x, top, center.x, top + h * 0.28f)
            close()
        }
        val echo = Stroke(strokePx(strokeWidth, d * 0.04f))
        val pivot = Offset(center.x, center.y)
        onDrawBehind {
            val p = motion.progress
            val beat = 1f + 0.16f * bump(p, 0f, 0.14f) + 0.1f * bump(p, 0.2f, 0.12f)
            if (showEcho && p < 0.6f) {
                val t = p / 0.6f
                scale(1f + 0.45f * t, pivot) { drawPath(heart, color.copy(alpha = color.alpha * 0.5f * (1f - t)), style = echo) }
            }
            scale(beat, pivot) { drawPath(heart, color) }
        }
    }
}

/** A smooth 0 to 1 to 0 bump starting at [start] and lasting [length]. */
private fun bump(t: Float, start: Float, length: Float): Float {
    val x = (t - start) / length
    return if (x in 0f..1f) sin(x * Pi) else 0f
}
