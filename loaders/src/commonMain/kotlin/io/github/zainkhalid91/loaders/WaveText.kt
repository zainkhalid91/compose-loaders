package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * The letters of [text] hop one after another in a wave and brighten as they rise.
 * It sizes itself to the text. Unless [config] has its own label, screen readers say [text].
 */
@Composable
public fun WaveText(
    text: String = LoaderDefaults.label,
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    style: TextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    amplitude: Dp = 5.dp,
    durationMillis: Int = 1600,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val whole = remember(text, style, density) { measurer.measure(text, style) }
    // One layout per letter, placed where it sits in the whole word, so kerning stays right.
    val letters = remember(text, style, density) { text.map { measurer.measure(it.toString(), style) } }
    val lefts = remember(whole) { FloatArray(text.length) { whole.getHorizontalPosition(it, usePrimaryDirection = true) } }
    val spoken = if (config.label == LoaderDefaults.label) config.copy(label = text) else config
    val (width, height) = with(density) { whole.size.width.toDp() to whole.size.height.toDp() + amplitude * 2 }
    LoaderCanvas(modifier, width, height, durationMillis, progress, spoken, restPose = 0.12f) { motion ->
        val lift = amplitude.toPx()
        // Text keeps one colour so its layout is never restyled; each letter fades through a
        // reused layer paint instead, which allocates nothing per frame.
        val fade = Paint()
        val bounds = Array(letters.size) { Rect(0f, 0f, letters[it].size.width.toFloat(), letters[it].size.height.toFloat()) }
        onDrawBehind {
            val canvas = drawContext.canvas
            for (i in letters.indices) {
                if (text[i].isWhitespace()) continue
                val t = fract(motion.progress - i * 0.07f)
                val hop = if (t < 0.3f) sin(t / 0.3f * Pi) else 0f
                translate(lefts[i], lift * (1.5f - hop)) {
                    fade.alpha = 0.55f + 0.45f * hop
                    canvas.saveLayer(bounds[i], fade)
                    drawText(letters[i], color)
                    canvas.restore()
                }
            }
        }
    }
}
