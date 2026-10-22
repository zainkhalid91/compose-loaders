package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.cos

/** Equalizer bars stretching from their centre line in a travelling wave. Pass [brush] for gradient bars. */
@Composable
public fun WaveBars(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    bars: Int = 5,
    durationMillis: Int = 1000,
    brush: Brush? = null,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(bars >= 1) { "bars must be at least 1, was $bars" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config) { motion ->
        val h = this.size.height
        // Bars of width B separated by gaps of 0.6 * B fill the width.
        val barWidth = this.size.width / (bars + (bars - 1) * 0.6f)
        val corner = CornerRadius(barWidth / 2f)
        onDrawBehind {
            for (i in 0 until bars) {
                val wave = 0.5f - 0.5f * cos(TwoPi * (motion.progress - i * 0.5f / bars))
                val barHeight = h * (0.25f + 0.75f * wave)
                val topLeft = Offset(i * barWidth * 1.6f, (h - barHeight) / 2f)
                val barSize = Size(barWidth, barHeight)
                if (brush != null) {
                    drawRoundRect(brush, topLeft, barSize, corner)
                } else {
                    drawRoundRect(color, topLeft, barSize, corner)
                }
            }
        }
    }
}
