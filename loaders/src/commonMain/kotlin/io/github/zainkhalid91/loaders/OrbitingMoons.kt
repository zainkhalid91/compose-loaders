package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A planet with moons on tilted orbits at different speeds. Moons pass behind the planet and
 * shrink with distance, so the system reads as 3D.
 */
@Composable
public fun OrbitingMoons(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    moonColor: Color = color.copy(alpha = color.alpha * 0.7f),
    size: Dp = LoaderDefaults.size,
    moons: Int = 2,
    showOrbits: Boolean = true,
    durationMillis: Int = 2400,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(moons in 1..3) { "moons must be 1 to 3, was $moons" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.15f) { motion ->
        val r = this.size.minDimension / 2f
        val planet = r * 0.34f
        val moon = r * 0.13f
        val orbitStyle = Stroke((r * 0.025f).coerceAtLeast(1f))
        onDrawBehind {
            // Behind the planet first, then the planet, then whatever is in front of it.
            for (pass in 0..2) {
                if (pass == 1) {
                    drawCircle(color, planet)
                    continue
                }
                for (i in 0 until moons) {
                    val a = r * (0.62f + 0.19f * i) - moon
                    val b = a * 0.42f
                    if (pass == 0 && showOrbits) {
                        drawOval(color.copy(alpha = color.alpha * 0.2f), Offset(center.x - a, center.y - b), Size(a * 2f, b * 2f), style = orbitStyle)
                    }
                    // Inner moons go round faster; whole turns per loop keep it seamless.
                    val t = TwoPi * (motion.progress * (moons - i) + i * 0.37f)
                    val front = sin(t) > 0f
                    if (front == (pass == 2)) moon(moonColor, Offset(center.x + a * cos(t), center.y + b * sin(t)), moon, sin(t))
                }
            }
        }
    }
}

private fun DrawScope.moon(color: Color, at: Offset, radius: Float, depth: Float) {
    val near = (depth + 1f) / 2f
    drawCircle(color.copy(alpha = color.alpha * (0.55f + 0.45f * near)), radius * (0.7f + 0.3f * near), at)
}
