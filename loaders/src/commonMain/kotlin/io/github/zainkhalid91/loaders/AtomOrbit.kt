package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/** Electrons circling a pulsing nucleus on three tilted elliptical orbits. */
@Composable
public fun AtomOrbit(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1500,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config) { motion ->
        val r = this.size.minDimension / 2f
        val electron = r * 0.1f
        val a = r - electron
        val b = a * 0.36f
        val orbitStyle = Stroke((r * 0.03f).coerceAtLeast(1f))
        val orbitTopLeft = Offset(center.x - a, center.y - b)
        val orbitSize = Size(a * 2f, b * 2f)
        onDrawBehind {
            val p = motion.progress
            for (i in 0 until 3) {
                rotate(i * 60f) {
                    drawOval(color.copy(alpha = color.alpha * 0.3f), orbitTopLeft, orbitSize, style = orbitStyle)
                    val t = TwoPi * (p + i / 3f)
                    drawCircle(color, electron, Offset(center.x + a * cos(t), center.y + b * sin(t)))
                }
            }
            drawCircle(color, r * 0.16f * (1f + 0.15f * sin(p * TwoPi * 2f)))
        }
    }
}
