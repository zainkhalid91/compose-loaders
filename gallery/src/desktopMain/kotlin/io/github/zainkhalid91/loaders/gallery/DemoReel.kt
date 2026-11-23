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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zainkhalid91.loaders.CometSpinner
import io.github.zainkhalid91.loaders.DualRing
import io.github.zainkhalid91.loaders.FreezeLoaders
import io.github.zainkhalid91.loaders.LoaderConfig
import io.github.zainkhalid91.loaders.LoaderState
import io.github.zainkhalid91.loaders.LoadingButton
import io.github.zainkhalid91.loaders.PathLoader
import io.github.zainkhalid91.loaders.PathLoaderStyle
import io.github.zainkhalid91.loaders.ProvideLoaderDefaults
import io.github.zainkhalid91.loaders.ResultLoader
import io.github.zainkhalid91.loaders.rememberLoadingVisibility
import java.io.File
import kotlin.math.roundToInt

private const val ReelFps = 30

// Scene lengths in seconds.
private val Scenes = floatArrayOf(5.6f, 3.4f, 4.2f, 5.4f, 4.8f, 3.8f, 3.4f)

private val Green = Color(0xFF34D399)
private val Red = Color(0xFFF87171)

/**
 * Renders a 1080 x 1350 (4:5) demo of 0.3.0, frame by frame, from the real library on a
 * virtual clock. Usage: demoreel <outDir>. tools/make-gifs.sh turns the frames into
 * docs/media/demo-reel.mp4.
 */
fun main(args: Array<String>) {
    val out = File(args.getOrElse(0) { "build/demoreel" }).apply { mkdirs() }
    val total = (Scenes.sum() * ReelFps).roundToInt()
    val time = mutableFloatStateOf(0f)
    VirtualScene(1080, 1350, 3f) { Reel(time.floatValue) }.use { scene ->
        for (i in 0 until total) {
            time.floatValue = i / ReelFps.toFloat()
            scene.render(i * 1_000_000_000L / ReelFps).writePng(File(out, "%04d.png".format(i)))
        }
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
    val s = t - start
    val title: String
    val code: String
    val stage: @Composable () -> Unit
    when (index) {
        0 -> {
            title = "Button becomes its loader"
            code = "LoadingButton(text = \"Place order\",\n    state = state, onClick = { placeOrder() })"
            stage = { ButtonScene(s) }
        }
        1 -> {
            title = "No double submits"
            code = "// Taps are ignored until state is Idle again"
            stage = { DoubleTapScene(s) }
        }
        2 -> {
            title = "Loaders that end in a result"
            code = "ResultLoader(state) { CometSpinner() }"
            stage = { ResultScene(s) }
        }
        3 -> {
            title = "No flash on fast requests"
            code = "val show by rememberLoadingVisibility(isLoading)\nif (show) CometSpinner()"
            stage = { FlickerScene(s) }
        }
        4 -> {
            title = "Your logo as a loader"
            code = "PathLoader(YourLogo, style = Fill, progress = upload)"
            stage = { LogoScene(s) }
        }
        5 -> {
            title = "Stable screenshot tests"
            code = "paparazzi.snapshot { FreezeLoaders { Screen() } }"
            stage = { FreezeScene() }
        }
        else -> {
            title = "Compose Loaders 0.3.0"
            code = "implementation(\"com.github.zainkhalid91.compose-loaders:loaders:0.3.0\")"
            stage = { OutroScene(s) }
        }
    }
    ProvideLoaderDefaults(config = LoaderConfig(announce = false)) {
        Column(
            Modifier.fillMaxSize().background(Background).padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText("Compose Loaders 0.3.0", style = TextStyle(color = Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(8.dp))
            BasicText(title, style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(236.dp)
                    .background(Card, RoundedCornerShape(24.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // A fresh composition per scene, so each one starts from its first frame.
                key(index) { stage() }
            }
            Spacer(Modifier.height(16.dp))
            BasicText(
                code,
                Modifier.fillMaxWidth().background(Color(0xFF0B0D11), RoundedCornerShape(14.dp)).padding(14.dp),
                style = TextStyle(color = Color(0xFFD7DBE6), fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp),
            )
            Spacer(Modifier.weight(1f))
            BasicText("Android + Desktop · open source · JitPack", style = TextStyle(color = Muted, fontSize = 12.sp))
        }
    }
}

@Composable
private fun Caption(text: String, color: Color = Muted) {
    BasicText(text, style = TextStyle(color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium))
}

@Composable
private fun ButtonScene(s: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
        LoadingButton(
            text = "Place order",
            state = timeline(s, 0.5f, 2.3f, 3.8f, LoaderState.Success),
            onClick = {},
            containerColor = GalleryPalette[0],
            height = 52.dp,
            haptics = false,
            modifier = Modifier.width(200.dp).tap(s, 0.5f),
        )
        LoadingButton(
            text = "Pay now",
            state = timeline(s, 1.0f, 2.8f, 4.3f, LoaderState.Error),
            onClick = {},
            containerColor = Color(0xFF0EA5E9),
            height = 52.dp,
            haptics = false,
            modifier = Modifier.width(200.dp).tap(s, 1.0f),
        )
    }
}

private val DoubleTaps = floatArrayOf(0.4f, 0.9f, 1.3f, 1.65f, 2.0f, 2.3f)

@Composable
private fun DoubleTapScene(s: Float) {
    val taps = DoubleTaps.count { s >= it }
    val last = DoubleTaps.lastOrNull { s >= it } ?: -1f
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        LoadingButton(
            text = "Place order",
            state = if (s < 0.4f) LoaderState.Idle else LoaderState.Loading,
            onClick = {},
            containerColor = GalleryPalette[0],
            height = 52.dp,
            haptics = false,
            modifier = Modifier.width(200.dp).tap(s, last),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Caption("Taps: $taps", Color.White)
            Caption("Orders sent: ${if (taps > 0) 1 else 0}", Green)
        }
    }
}

@Composable
private fun ResultScene(s: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
        ResultLoader(timeline(s, 0f, 1.2f, 99f, LoaderState.Success), color = GalleryPalette[0], size = 64.dp, haptics = false)
        ResultLoader(timeline(s, 0f, 1.6f, 99f, LoaderState.Success), color = GalleryPalette[1], size = 64.dp, haptics = false) {
            CometSpinner(color = GalleryPalette[1], size = 64.dp)
        }
        ResultLoader(timeline(s, 0f, 2.0f, 99f, LoaderState.Error), color = GalleryPalette[2], size = 64.dp, haptics = false) {
            DualRing(color = GalleryPalette[2], size = 64.dp)
        }
    }
}

@Composable
private fun FlickerScene(s: Float) {
    val fast = s in 0.7f..0.85f
    val slow = s in 2.6f..4.2f
    val loading = fast || slow
    val smart by rememberLoadingVisibility(loading)
    val status = when {
        s < 0.85f -> "Fast request: 150 ms"
        s < 2.6f -> "Fast request: 150 ms, done"
        s <= 4.2f -> "Slow request: 1.6 s"
        else -> "Slow request: 1.6 s, done"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Caption(status, Color.White)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FlickerPanel("if (isLoading)", loading)
            FlickerPanel("rememberLoadingVisibility", smart)
        }
    }
}

@Composable
private fun FlickerPanel(label: String, show: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(140.dp, 110.dp).background(Background, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            if (show) CometSpinner(color = GalleryPalette[0], size = 44.dp)
        }
        Spacer(Modifier.height(8.dp))
        Caption(label)
    }
}

@Composable
private fun LogoScene(s: Float) {
    val upload = (s / 3.6f).coerceIn(0f, 1f)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        LogoCell("Trace") { PathLoader(DemoShapes.Badge, 24f, 24f, size = 56.dp, style = PathLoaderStyle.Trace, color = GalleryPalette[0]) }
        LogoCell("Fill") { PathLoader(DemoShapes.Cloud, 24f, 24f, size = 56.dp, style = PathLoaderStyle.Fill, color = GalleryPalette[1]) }
        LogoCell("Shimmer") { PathLoader(DemoShapes.Bolt, 24f, 24f, size = 56.dp, style = PathLoaderStyle.Shimmer, color = GalleryPalette[3]) }
        LogoCell("${(upload * 100).roundToInt()}%") {
            PathLoader(DemoShapes.Cloud, 24f, 24f, size = 56.dp, style = PathLoaderStyle.Fill, color = GalleryPalette[4], progress = upload)
        }
    }
}

@Composable
private fun LogoCell(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(68.dp)) {
        content()
        Spacer(Modifier.height(10.dp))
        Caption(label)
    }
}

@Composable
private fun FreezeScene() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FreezePanel("Without", 0.12f, 0.58f, "Screenshots differ", Red)
        FreezePanel("FreezeLoaders", null, null, "Identical every run", Green)
    }
}

@Composable
private fun FreezePanel(title: String, first: Float?, second: Float?, verdict: String, color: Color) {
    Column(
        Modifier.background(Background, RoundedCornerShape(14.dp)).padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Caption(title, Color.White)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            for ((run, frame) in listOf(1 to first, 2 to second)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FreezeLoaders(frame) { CometSpinner(color = GalleryPalette[1], size = 44.dp) }
                    Spacer(Modifier.height(6.dp))
                    Caption("Run $run")
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Caption(verdict, color)
    }
}

@Composable
private fun OutroScene(s: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp), verticalAlignment = Alignment.CenterVertically) {
            PathLoader(DemoShapes.Badge, 24f, 24f, size = 56.dp, color = GalleryPalette[0])
            ResultLoader(timeline(s, 0f, 1.0f, 99f, LoaderState.Success), color = GalleryPalette[1], size = 56.dp, haptics = false)
            LoadingButton(
                text = "Ship it",
                state = timeline(s, 0.3f, 1.4f, 99f, LoaderState.Success),
                onClick = {},
                containerColor = GalleryPalette[2],
                haptics = false,
                modifier = Modifier.width(130.dp),
            )
        }
        Caption("46 loaders · LoadingButton · ResultLoader · PathLoader", Color.White)
        Caption("Pure Compose. No Lottie, no assets.")
    }
}
