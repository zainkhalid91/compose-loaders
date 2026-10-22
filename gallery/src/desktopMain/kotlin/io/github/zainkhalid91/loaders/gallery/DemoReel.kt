package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zainkhalid91.loaders.CometSpinner
import io.github.zainkhalid91.loaders.DnaHelix
import io.github.zainkhalid91.loaders.ExpressiveArc
import io.github.zainkhalid91.loaders.FireworkBurst
import io.github.zainkhalid91.loaders.Hourglass
import io.github.zainkhalid91.loaders.LoaderConfig
import io.github.zainkhalid91.loaders.PacDots
import io.github.zainkhalid91.loaders.RadarSweep
import io.github.zainkhalid91.loaders.ReducedMotion
import io.github.zainkhalid91.loaders.SlidingBar
import io.github.zainkhalid91.loaders.WaveBars
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.math.roundToInt

private const val ReelFps = 30
private const val SceneSeconds = 3.5f

/**
 * Renders a 1080 x 1350 (4:5) demo of the 0.2.0 features, frame by frame, from the real library.
 * Usage: demoreel <outDir>. Turn the frames into a video with tools/make-gifs.sh.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) {
    val out = File(args.getOrElse(0) { "build/demoreel" }).apply { mkdirs() }
    val total = (SceneSeconds * 5 * ReelFps).roundToInt()
    var t = 0f
    val time = androidx.compose.runtime.mutableFloatStateOf(0f)
    val scene = ImageComposeScene(1080, 1350, Density(3f)) { Reel(time.floatValue) }
    try {
        for (i in 0 until total) {
            t = i / ReelFps.toFloat()
            time.floatValue = t
            val image = scene.render(i * 1_000_000_000L / ReelFps)
            File(out, "%04d.png".format(i)).writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
        }
    } finally {
        scene.close()
    }
    println("Wrote $total frames to ${out.absolutePath}")
}

@Composable
private fun Reel(t: Float) {
    val sceneIndex = (t / SceneSeconds).toInt().coerceAtMost(4)
    val local = (t - sceneIndex * SceneSeconds) / SceneSeconds
    val quiet = LoaderConfig(announce = false)
    val (title, code) = when (sceneIndex) {
        0 -> {
            val hue = local * 300f
            "Any colour" to "CometSpinner(color = Color.hsv(${hue.roundToInt()}f, 0.62f, 1f))"
        }
        1 -> "Any speed" to "WaveBars(config = LoaderConfig(speed = ${speedAt(local).fmt()}f))"
        2 -> "Stroke width + gradient" to "ExpressiveArc(strokeWidth = ${strokeAt(local).roundToInt()}.dp, brush = gradient)"
        3 -> "Reduced motion: never frozen" to "DnaHelix(config = LoaderConfig(reducedMotion = On))"
        else -> "5 new loaders this week" to "RadarSweep · Hourglass · PacDots · SlidingBar · FireworkBurst"
    }
    Column(
        Modifier.fillMaxSize().background(Background).padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText("Compose Loaders 0.2.0", style = TextStyle(color = Muted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(10.dp))
        BasicText(title, style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(22.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Card, RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            when (sceneIndex) {
                0 -> CometSpinner(color = Color.hsv(local * 300f, 0.62f, 1f), size = 110.dp, config = quiet)
                1 -> WaveBars(color = GalleryPalette[2], size = 110.dp, config = quiet.copy(speed = speedAt(local)))
                2 -> ExpressiveArc(
                    size = 110.dp,
                    strokeWidth = strokeAt(local).dp,
                    brush = Brush.sweepGradient(listOf(GalleryPalette[1], GalleryPalette[0], GalleryPalette[2], GalleryPalette[1])),
                    config = quiet,
                )
                3 -> DnaHelix(color = GalleryPalette[0], size = 220.dp, config = quiet.copy(reducedMotion = ReducedMotion.On))
                else -> NewThisWeek(quiet)
            }
        }
        Spacer(Modifier.height(22.dp))
        BasicText(
            code,
            Modifier.fillMaxWidth().background(Color(0xFF0B0D11), RoundedCornerShape(14.dp)).padding(16.dp),
            style = TextStyle(color = Color(0xFFD7DBE6), fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp),
        )
        Spacer(Modifier.height(18.dp))
        BasicText(
            "Pure Compose · no assets · screen reader ready",
            style = TextStyle(color = Muted, fontSize = 13.sp),
        )
    }
}

@Composable
private fun NewThisWeek(config: LoaderConfig) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp), verticalAlignment = Alignment.CenterVertically) {
            RadarSweep(color = GalleryPalette[4], size = 64.dp, config = config)
            Hourglass(color = GalleryPalette[3], size = 64.dp, config = config)
            FireworkBurst(color = GalleryPalette[2], size = 64.dp, config = config)
        }
        PacDots(color = GalleryPalette[3], size = 120.dp, config = config)
        SlidingBar(color = GalleryPalette[1], width = 220.dp, config = config)
    }
}

private fun speedAt(local: Float) = 0.5f + 2.5f * local

private fun strokeAt(local: Float) = 3f + 15f * local

private fun Float.fmt(): String = ((this * 10).roundToInt() / 10f).toString()
