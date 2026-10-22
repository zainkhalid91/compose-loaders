package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/** A round flask whose two wavy liquid layers rise and fall. */
@Composable
public fun LiquidFill(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 2400,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.3f) { motion ->
        val r = this.size.minDimension / 2f
        val stroke = strokePx(strokeWidth, r * 0.08f)
        val inner = r - stroke * 1.8f
        val outline = Stroke(stroke)
        val clip = Path().apply { addOval(Rect(center, inner)) }
        val wave = Path()
        onDrawBehind {
            drawCircle(color, r - stroke / 2f, style = outline)
            val level = 0.25f + 0.5f * (0.5f - 0.5f * cos(motion.progress * TwoPi))
            val waterline = center.y + inner - level * inner * 2f
            // Whole turns of phase per loop keep the waves seamless.
            val phase = motion.progress * TwoPi * 2f
            clipPath(clip) {
                liquid(wave, color.copy(alpha = color.alpha * 0.45f), waterline, inner, -phase + 1.2f)
                liquid(wave, color, waterline, inner, phase)
            }
        }
    }
}

private fun DrawScope.liquid(path: Path, color: Color, waterline: Float, inner: Float, phase: Float) {
    val left = center.x - inner
    val right = center.x + inner
    val amplitude = inner * 0.09f
    val wavelength = inner * 1.6f
    path.rewind()
    path.moveTo(left, center.y + inner)
    var x = left
    while (x <= right) {
        path.lineTo(x, waterline + amplitude * sin(TwoPi * (x - left) / wavelength + phase))
        x += 2f
    }
    path.lineTo(right, waterline + amplitude * sin(TwoPi * (right - left) / wavelength + phase))
    path.lineTo(right, center.y + inner)
    path.close()
    drawPath(path, color)
}
