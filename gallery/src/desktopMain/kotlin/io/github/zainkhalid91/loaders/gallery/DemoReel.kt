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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zainkhalid91.loaders.DnaHelix
import io.github.zainkhalid91.loaders.GearPair
import io.github.zainkhalid91.loaders.GooeyOrbit
import io.github.zainkhalid91.loaders.HeartPulse
import io.github.zainkhalid91.loaders.LoaderConfig
import io.github.zainkhalid91.loaders.NeonTrace
import io.github.zainkhalid91.loaders.ProvideLoaderDefaults
import io.github.zainkhalid91.loaders.ReducedMotion
import io.github.zainkhalid91.loaders.SegmentedRing
import io.github.zainkhalid91.loaders.SpringCoil
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.math.roundToInt

private const val ReelFps = 30

// Scene lengths in seconds; the first shows every new loader, so it stays up longer.
private val Scenes = floatArrayOf(5f, 3f, 3f, 3.2f, 3f, 3f, 3f, 3.4f)

/**
 * Renders a 1080 x 1350 (4:5) demo of 0.2.0, frame by frame, from the real library.
 * Usage: demoreel <outDir>. tools/make-gifs.sh turns the frames into docs/media/demo-reel.mp4.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) {
    val out = File(args.getOrElse(0) { "build/demoreel" }).apply { mkdirs() }
    val total = (Scenes.sum() * ReelFps).roundToInt()
    val time = mutableFloatStateOf(0f)
    val scene = ImageComposeScene(1080, 1350, Density(3f)) { Reel(time.floatValue) }
    try {
        for (i in 0 until total) {
            time.floatValue = i / ReelFps.toFloat()
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
    var start = 0f
    var index = 0
    while (index < Scenes.size - 1 && t >= start + Scenes[index]) {
        start += Scenes[index]
        index++
    }
    val local = ((t - start) / Scenes[index]).coerceIn(0f, 1f)
    val quiet = LoaderConfig(announce = false)
    val title: String
    val code: String
    val stage: @Composable () -> Unit
    when (index) {
        0 -> {
            title = "20 new loaders"
            code = "45 loaders · pure Compose · no assets"
            stage = { NewLoaderGrid() }
        }
        1 -> {
            val hue = local * 300f
            title = "Any colour"
            code = "GooeyOrbit(color = Color.hsv(${hue.roundToInt()}f, 0.62f, 1f))"
            stage = { GooeyOrbit(color = Color.hsv(hue, 0.62f, 1f), size = 120.dp, config = quiet) }
        }
        2 -> {
            val speed = 0.4f + 2.6f * local
            title = "Any speed"
            code = "GearPair(config = LoaderConfig(speed = ${speed.fmt()}f))"
            stage = { GearPair(color = GalleryPalette[3], size = 120.dp, config = quiet.copy(speed = speed)) }
        }
        3 -> {
            val reverse = local >= 0.5f
            title = "Run it backwards"
            code = "SegmentedRing(config = LoaderConfig(reverse = $reverse))"
            stage = { SegmentedRing(color = GalleryPalette[1], size = 120.dp, config = quiet.copy(reverse = reverse)) }
        }
        4 -> {
            val stroke = 2f + 8f * local
            title = "Stroke width"
            code = "NeonTrace(strokeWidth = ${stroke.roundToInt()}.dp)"
            stage = { NeonTrace(color = GalleryPalette[2], size = 120.dp, strokeWidth = stroke.dp, config = quiet) }
        }
        5 -> {
            val coils = 3 + (local * 9).toInt()
            title = "Counts and shapes"
            code = "SpringCoil(coils = $coils)"
            stage = { SpringCoil(color = GalleryPalette[4], size = 130.dp, coils = coils, config = quiet) }
        }
        6 -> {
            title = "Reduced motion: never frozen"
            code = "DnaHelix(config = LoaderConfig(reducedMotion = On))"
            stage = { DnaHelix(color = GalleryPalette[0], size = 220.dp, config = quiet.copy(reducedMotion = ReducedMotion.On)) }
        }
        else -> {
            val percent = (local * 100).roundToInt().coerceAtMost(100)
            title = "Screen reader ready"
            code = "HeartPulse(progress = ${(percent / 100f).fmt()}f, config = LoaderConfig(label = \"Loading photos\"))"
            stage = { ScreenReaderStage(percent, quiet) }
        }
    }
    val tall = index == 0
    Column(
        Modifier.fillMaxSize().background(Background).padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText("Compose Loaders 0.2.0", style = TextStyle(color = Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(8.dp))
        BasicText(title, style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (tall) 262.dp else 210.dp)
                .background(Card, RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) { stage() }
        Spacer(Modifier.height(16.dp))
        BasicText(
            code,
            Modifier.fillMaxWidth().background(Color(0xFF0B0D11), RoundedCornerShape(14.dp)).padding(14.dp),
            style = TextStyle(color = Color(0xFFD7DBE6), fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp),
        )
        Spacer(Modifier.weight(1f))
        BasicText(
            "Android + Desktop · open source · JitPack",
            style = TextStyle(color = Muted, fontSize = 12.sp),
        )
    }
}

/** The 20 loaders added in this release, four across, in the gallery palette. */
@Composable
private fun NewLoaderGrid() {
    val fresh = LoaderCatalog.entries.filter { it.batch == "2026-W45" }
    ProvideLoaderDefaults(config = LoaderConfig(announce = false)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in fresh.chunked(4)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((column, entry) in row.withIndex()) {
                        Box(Modifier.size(width = 74.dp, height = 47.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.graphicsLayer(scaleX = 0.78f, scaleY = 0.78f)) {
                                entry.content(GalleryPalette[(entry.id + column) % GalleryPalette.size])
                            }
                        }
                    }
                }
            }
        }
    }
}

/** What a screen reader hears, shown as a caption under the loader. */
@Composable
private fun ScreenReaderStage(percent: Int, config: LoaderConfig) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HeartPulse(color = GalleryPalette[2], size = 100.dp, progress = percent / 100f, config = config.copy(label = "Loading photos"))
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.background(Color(0xFF2B2F3A), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).background(GalleryPalette[4], RoundedCornerShape(50)))
            Spacer(Modifier.width(8.dp))
            BasicText("TalkBack: \"Loading photos, $percent percent\"", style = TextStyle(color = Color.White, fontSize = 12.sp))
        }
    }
}

private fun Float.fmt(): String = ((this * 10).roundToInt() / 10f).toString()
