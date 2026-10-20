package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.cos

/** Equalizer bars stretching from their centre line in a travelling wave. */
@Composable
public fun WaveBars(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size,
    bars: Int = 5,
    durationMillis: Int = 1000,
) {
    require(bars >= 1) { "bars must be at least 1, was $bars" }
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val w = this.size.width
        val h = this.size.height
        // Bars of width B separated by gaps of 0.6 * B fill the width.
        val barWidth = w / (bars + (bars - 1) * 0.6f)
        for (i in 0 until bars) {
            val wave = 0.5f - 0.5f * cos(TwoPi * (progress - i * 0.5f / bars))
            val barHeight = h * (0.25f + 0.75f * wave)
            drawRoundRect(
                color = color,
                topLeft = Offset(i * barWidth * 1.6f, (h - barHeight) / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f),
            )
        }
    }
}
