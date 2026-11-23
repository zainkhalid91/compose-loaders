package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zainkhalid91.loaders.CometSpinner
import io.github.zainkhalid91.loaders.DualRing
import io.github.zainkhalid91.loaders.LoaderConfig
import io.github.zainkhalid91.loaders.LoaderState
import io.github.zainkhalid91.loaders.LoadingButton
import io.github.zainkhalid91.loaders.PathLoader
import io.github.zainkhalid91.loaders.PathLoaderStyle
import io.github.zainkhalid91.loaders.ProvideLoaderDefaults
import io.github.zainkhalid91.loaders.ResultLoader
import java.io.File

private const val PatternFps = 30

/** Renders the README pattern demos. Usage: recordPatterns <outDir>. */
fun main(args: Array<String>) {
    val out = File(args.getOrElse(0) { "build/patterns" })
    record(File(out, "loading-button"), 760, 180, 5.0f) { t -> ButtonDemo(t) }
    record(File(out, "result-loader"), 620, 190, 3.6f) { t -> ResultDemo(t) }
    record(File(out, "path-loader"), 1180, 230, 2.4f) { t -> LogoDemo(t) }
    println("Wrote pattern frames to ${out.absolutePath}")
}

private fun record(dir: File, width: Int, height: Int, seconds: Float, content: @Composable (Float) -> Unit) {
    dir.mkdirs()
    val time = mutableFloatStateOf(0f)
    val frames = (seconds * PatternFps).toInt()
    VirtualScene(width, height, 2f) {
        ProvideLoaderDefaults(config = LoaderConfig(announce = false)) {
            Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) { content(time.floatValue) }
        }
    }.use { scene ->
        for (i in 0 until frames) {
            time.floatValue = i / PatternFps.toFloat()
            scene.render(i * 1_000_000_000L / PatternFps).writePng(File(dir, "%03d.png".format(i)))
        }
    }
}

/** State for a timeline: Idle, then Loading from [start], [result] from [end], Idle again from [reset]. */
internal fun timeline(t: Float, start: Float, end: Float, reset: Float, result: LoaderState): LoaderState = when {
    t < start -> LoaderState.Idle
    t < end -> LoaderState.Loading
    t < reset -> result
    else -> LoaderState.Idle
}

/** A soft ring where a finger tapped, fading out over 0.4 s. */
internal fun Modifier.tap(t: Float, at: Float): Modifier = drawWithContent {
    drawContent()
    val k = (t - at) / 0.4f
    if (k in 0f..1f) {
        drawCircle(Color.White.copy(alpha = 0.35f * (1f - k)), radius = size.height * (0.25f + 0.45f * k))
    }
}

@Composable
private fun ButtonDemo(t: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
        LoadingButton(
            text = "Place order",
            state = timeline(t, 0.4f, 2.2f, 3.6f, LoaderState.Success),
            onClick = {},
            containerColor = GalleryPalette[0],
            haptics = false,
            modifier = Modifier.width(170.dp).tap(t, 0.4f),
        )
        LoadingButton(
            text = "Pay now",
            state = timeline(t, 0.9f, 2.6f, 4.0f, LoaderState.Error),
            onClick = {},
            containerColor = Color(0xFF0EA5E9),
            haptics = false,
            modifier = Modifier.width(150.dp).tap(t, 0.9f),
        )
    }
}

@Composable
private fun ResultDemo(t: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(48.dp), verticalAlignment = Alignment.CenterVertically) {
        ResultLoader(timeline(t, 0f, 1.3f, 3.5f, LoaderState.Success), color = GalleryPalette[0], size = 56.dp, haptics = false)
        ResultLoader(timeline(t, 0f, 1.6f, 3.5f, LoaderState.Success), color = GalleryPalette[1], size = 56.dp, haptics = false) {
            CometSpinner(color = GalleryPalette[1], size = 56.dp)
        }
        ResultLoader(timeline(t, 0f, 1.9f, 3.5f, LoaderState.Error), color = GalleryPalette[2], size = 56.dp, haptics = false) {
            DualRing(color = GalleryPalette[2], size = 56.dp)
        }
    }
}

@Composable
private fun LogoDemo(t: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(36.dp), verticalAlignment = Alignment.Bottom) {
        Labelled("Trace") { PathLoader(DemoShapes.Badge, 24f, 24f, style = PathLoaderStyle.Trace, color = GalleryPalette[0]) }
        Labelled("Fill") { PathLoader(DemoShapes.Cloud, 24f, 24f, style = PathLoaderStyle.Fill, color = GalleryPalette[1]) }
        Labelled("Shimmer") { PathLoader(DemoShapes.Bolt, 24f, 24f, style = PathLoaderStyle.Shimmer, color = GalleryPalette[3]) }
        val upload = (t / 2.2f).coerceAtMost(1f)
        Labelled("Upload ${(upload * 100).toInt()}%") {
            PathLoader(DemoShapes.Cloud, 24f, 24f, style = PathLoaderStyle.Fill, color = GalleryPalette[4], progress = upload)
        }
    }
}

@Composable
private fun Labelled(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(110.dp)) {
        content()
        Spacer(Modifier.height(10.dp))
        BasicText(label, style = TextStyle(color = Muted, fontSize = 12.sp))
    }
}
