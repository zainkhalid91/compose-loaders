package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A pill-shaped bar with diagonal candy stripes scrolling along it. */
@Composable
public fun StripedBar(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    stripeColor: Color = Color.White.copy(alpha = 0.28f),
    width: Dp = 160.dp,
    height: Dp = 14.dp,
    durationMillis: Int = 700,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, width, height, durationMillis, progress, config) { motion ->
        val h = this.size.height
        val period = h * 2f
        val pill = Path().apply { addRoundRect(RoundRect(0f, 0f, this@LoaderCanvas.size.width, h, CornerRadius(h / 2f))) }
        val stripes = Path()
        var x = -period * 2f
        while (x < this.size.width + period) {
            stripes.moveTo(x, h)
            stripes.lineTo(x + h, h)
            stripes.lineTo(x + h * 2f, 0f)
            stripes.lineTo(x + h, 0f)
            stripes.close()
            x += period
        }
        onDrawBehind {
            clipPath(pill) {
                drawRect(color)
                // Shifting by exactly one period per loop keeps the scroll seamless.
                translate(left = motion.progress * period) { drawPath(stripes, stripeColor) }
            }
        }
    }
}
