package io.github.zainkhalid91.loaders.benchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import io.github.zainkhalid91.loaders.ExpressiveArc
import io.github.zainkhalid91.loaders.LoaderConfig

/**
 * Shows a grid of identical spinners: Compose Loaders' ExpressiveArc, or an equivalent Lottie arc
 * (res/raw/arc.json: one stroked ellipse with animated trim paths and rotation).
 *
 * adb shell am start -n io.github.zainkhalid91.loaders.benchmark/.BenchActivity --es mode lottie --ei count 24
 */
class BenchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra("mode") ?: "compose"
        val count = intent.getIntExtra("count", 24)
        setContent { Grid(count) { if (mode == "lottie") LottieArc() else ComposeArc() } }
    }
}

private val Quiet = LoaderConfig(announce = false)

@Composable
private fun Grid(count: Int, cell: @Composable () -> Unit) {
    LazyVerticalGrid(GridCells.Fixed(4), Modifier.fillMaxSize()) {
        items(count) {
            Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) { cell() }
        }
    }
}

@Composable
private fun ComposeArc() {
    ExpressiveArc(size = 56.dp, config = Quiet)
}

@Composable
private fun LottieArc() {
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.arc)).value
    LottieAnimation(composition, Modifier.size(56.dp), iterations = LottieConstants.IterateForever)
}
