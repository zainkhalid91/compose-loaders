package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import kotlin.math.sin

// Fixed pseudo-random sideways drift for each flame particle and star column.
private val Drift = floatArrayOf(-0.6f, 0.3f, -0.15f, 0.7f, -0.45f, 0.1f, 0.5f, -0.3f, 0.2f, -0.7f, 0.4f, -0.05f)
private val StarColumns = floatArrayOf(0.12f, 0.82f, 0.3f, 0.68f, 0.9f, 0.2f)

/** A little rocket bobbing upwards with a flickering exhaust while stars stream past. */
@Composable
public fun Rocket(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    flameColor: Color = Color(0xFFFFA94D),
    windowColor: Color = Color.White,
    starColor: Color = color.copy(alpha = color.alpha * 0.45f),
    size: Dp = LoaderDefaults.size,
    particles: Int = 10,
    durationMillis: Int = 1200,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(particles in 0..Drift.size) { "particles must be 0 to ${Drift.size}, was $particles" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.2f) { motion ->
        val d = this.size.minDimension
        val cx = center.x
        val bodyW = d * 0.28f
        val bodyH = d * 0.56f
        val top = d * 0.04f
        val bottom = top + bodyH
        val body = Path().apply {
            moveTo(cx, top)
            cubicTo(cx + bodyW * 0.75f, top + bodyH * 0.25f, cx + bodyW / 2f, top + bodyH * 0.7f, cx + bodyW / 2f, bottom)
            lineTo(cx - bodyW / 2f, bottom)
            cubicTo(cx - bodyW / 2f, top + bodyH * 0.7f, cx - bodyW * 0.75f, top + bodyH * 0.25f, cx, top)
            close()
        }
        val fins = Path().apply {
            moveTo(cx - bodyW / 2f, bottom - bodyH * 0.3f)
            lineTo(cx - bodyW * 1.05f, bottom + bodyH * 0.08f)
            lineTo(cx - bodyW / 2f, bottom)
            close()
            moveTo(cx + bodyW / 2f, bottom - bodyH * 0.3f)
            lineTo(cx + bodyW * 1.05f, bottom + bodyH * 0.08f)
            lineTo(cx + bodyW / 2f, bottom)
            close()
        }
        val window = Offset(cx, top + bodyH * 0.42f)
        val flameLength = d * 0.3f
        val streak = d * 0.08f
        onDrawBehind {
            val p = motion.progress
            // Stars fall past at two speeds; whole passes per loop keep it seamless.
            for (k in StarColumns.indices) {
                val y = fract(p * (1 + k % 2) + k / StarColumns.size.toFloat()) * (d + streak) - streak
                val x = StarColumns[k] * d
                drawLine(starColor, Offset(x, y), Offset(x, y + streak), d * 0.02f, StrokeCap.Round)
            }
            val bob = d * 0.035f * sin(p * TwoPi * 2f)
            translate(top = bob) {
                rotate(4f * sin(p * TwoPi), pivot = Offset(cx, top + bodyH / 2f)) {
                    for (k in 0 until particles) {
                        val life = fract(p * 3f + k / particles.toFloat())
                        val at = Offset(cx + Drift[k] * bodyW * 0.45f * life, bottom + life * flameLength)
                        drawCircle(flameColor.copy(alpha = flameColor.alpha * (1f - life)), bodyW * 0.22f * (1f - 0.6f * life), at)
                    }
                    drawPath(fins, color.copy(alpha = color.alpha * 0.75f))
                    drawPath(body, color)
                    drawCircle(windowColor, bodyW * 0.17f, window)
                }
            }
        }
    }
}
