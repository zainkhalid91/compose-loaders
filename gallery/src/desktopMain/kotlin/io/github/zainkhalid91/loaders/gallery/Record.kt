package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zainkhalid91.loaders.ProvideLoaderDefaults
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

private const val Fps = 30
private val Dark = Color(0xFF0F1115)

/**
 * Renders loaders frame by frame on a virtual clock, for README media and visual checks.
 * Usage: record <outDir> [seconds]. Writes <outDir>/<Name>/NNN.png and <outDir>/all/NNN.png.
 */
fun main(args: Array<String>) {
    val out = File(args.getOrElse(0) { "build/frames" })
    val seconds = args.getOrNull(1)?.toFloat() ?: 2.4f
    val frames = (seconds * Fps).toInt()
    for (entry in LoaderCatalog.entries) {
        record(File(out, entry.name), 240, 240, frames) {
            Box(Modifier.fillMaxSize().background(Dark), contentAlignment = Alignment.Center) {
                entry.content(GalleryPalette.first())
            }
        }
    }
    record(File(out, "all"), 1000, 180 * ((LoaderCatalog.entries.size + 4) / 5) + 40, frames) { Sheet() }
    println("Wrote $frames frames per loader to ${out.absolutePath}")
}

@OptIn(ExperimentalComposeUiApi::class)
private fun record(dir: File, width: Int, height: Int, frames: Int, content: @Composable () -> Unit) {
    dir.mkdirs()
    val scene = ImageComposeScene(width, height, Density(2f)) { ProvideLoaderDefaults(config = GalleryConfig, content = content) }
    try {
        // Frame 0 starts the animations; the clock then advances in 1/Fps steps from there.
        for (i in 0..frames) {
            val image = scene.render(i * 1_000_000_000L / Fps)
            if (i == 0) continue
            val bytes = image.encodeToData(EncodedImageFormat.PNG)!!.bytes
            File(dir, "%03d.png".format(i - 1)).writeBytes(bytes)
        }
    } finally {
        scene.close()
    }
}

@Composable
private fun Sheet() {
    val rows = LoaderCatalog.entries.chunked(5)
    Column(
        Modifier.fillMaxSize().background(Dark).padding(12.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (row in rows) {
            Row(Modifier.fillMaxSize().weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                for ((index, entry) in row.withIndex()) {
                    Column(
                        Modifier.weight(1f).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(Modifier.size(width = 100.dp, height = 64.dp), contentAlignment = Alignment.Center) {
                            entry.content(GalleryPalette[(entry.id - 1 + index) % GalleryPalette.size])
                        }
                        BasicText(
                            entry.name,
                            Modifier.padding(top = 10.dp),
                            style = TextStyle(color = Color(0xFFB8BECC), fontSize = 10.sp),
                        )
                    }
                }
            }
        }
    }
}
