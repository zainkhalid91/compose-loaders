package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import io.github.zainkhalid91.loaders.LoaderConfig
import io.github.zainkhalid91.loaders.ProvideLoaderDefaults
import org.jetbrains.skia.Bitmap
import kotlin.math.pow

internal val Dark = Color(0xFF0F1115)

/** Renders [content] on a dark 240 x 240 px scene (density 2) and hands each frame to [onFrame]. */
@OptIn(ExperimentalComposeUiApi::class)
internal fun renderFrames(
    frames: Int,
    fps: Int,
    config: LoaderConfig = LoaderConfig(announce = false),
    content: @Composable () -> Unit,
    onFrame: (index: Int, scene: ImageComposeScene, render: () -> Unit) -> Unit,
) {
    val scene = ImageComposeScene(240, 240, Density(2f)) {
        ProvideLoaderDefaults(config = config) {
            Box(Modifier.fillMaxSize().background(Dark), contentAlignment = Alignment.Center) { content() }
        }
    }
    try {
        for (i in 0 until frames) onFrame(i, scene) { scene.render(i * 1_000_000_000L / fps) }
    } finally {
        scene.close()
    }
}

/** Relative luminance (WCAG) of each pixel in a BGRA frame, as one float per pixel. */
internal fun luminance(bitmap: Bitmap, out: FloatArray) {
    val bytes = bitmap.readPixels()!!
    for (i in out.indices) {
        val b = channel(bytes[i * 4])
        val g = channel(bytes[i * 4 + 1])
        val r = channel(bytes[i * 4 + 2])
        out[i] = 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
}

private fun channel(byte: Byte): Float {
    val c = (byte.toInt() and 0xFF) / 255f
    return if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
}
