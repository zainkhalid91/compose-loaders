package io.github.zainkhalid91.loaders.gallery

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zainkhalid91.loaders.CometSpinner
import io.github.zainkhalid91.loaders.LoaderState
import io.github.zainkhalid91.loaders.LoadingButton
import io.github.zainkhalid91.loaders.PathLoader
import io.github.zainkhalid91.loaders.PathLoaderStyle
import io.github.zainkhalid91.loaders.ResultLoader
import io.github.zainkhalid91.loaders.rememberLoadingVisibility
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Shapes for the PathLoader demos, as SVG path data on a 24 x 24 viewBox. */
public object DemoShapes {
    public const val Bolt: String = "M13 2 L4 14 L11 14 L10 22 L20 9 L13 9 Z"
    public const val Cloud: String =
        "M7 19 A5 5 0 0 1 6.3 9.05 A6 6 0 0 1 17.6 8.1 A5.5 5.5 0 0 1 17.5 19 Z"

    /** A ring with a hole and a dot: three outlines, to show multi-part shapes. */
    public const val Badge: String =
        "M12 2 A10 10 0 1 1 11.99 2 Z M12 6 A6 6 0 1 0 12.01 6 Z M12 10 A2 2 0 1 1 11.99 10 Z"
}

private val Accent = Color(0xFF7C5CFF)

/** Real-life patterns: the loading button, results, anti-flicker timing and logo loaders. */
@Composable
internal fun Patterns(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ButtonSection()
        ResultSection()
        FlickerSection()
        LogoSection()
    }
}

@Composable
private fun Section(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        BasicText(title, style = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(2.dp))
        BasicText(subtitle, style = TextStyle(color = Muted, fontSize = 13.sp))
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun Chip(text: String, onClick: () -> Unit) {
    BasicText(
        text,
        Modifier
            .border(1.dp, CardBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium),
    )
}

@Composable
private fun Caption(text: String) {
    BasicText(text, style = TextStyle(color = Muted, fontSize = 12.sp))
}

@Composable
private fun ButtonSection() {
    val scope = rememberCoroutineScope()
    var order by remember { mutableStateOf(LoaderState.Idle) }
    var pay by remember { mutableStateOf(LoaderState.Idle) }
    Section("LoadingButton", "Tap: it shrinks into a loader, then ends in a tick or a cross. Extra taps are ignored.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LoadingButton(
                text = "Place order",
                state = order,
                containerColor = Accent,
                onClick = {
                    order = LoaderState.Loading
                    scope.launch { delay(1800); order = LoaderState.Success }
                },
                onResultShown = { order = LoaderState.Idle },
            )
            LoadingButton(
                text = "Pay now",
                state = pay,
                containerColor = Color(0xFF0EA5E9),
                onClick = {
                    pay = LoaderState.Loading
                    scope.launch { delay(1500); pay = LoaderState.Error }
                },
                onResultShown = { pay = LoaderState.Idle },
            )
        }
        Spacer(Modifier.height(8.dp))
        Caption("\"Place order\" succeeds, \"Pay now\" fails.")
    }
}

@Composable
private fun ResultSection() {
    var state by remember { mutableStateOf(LoaderState.Loading) }
    Section("ResultLoader", "Any loader can end in a result instead of just disappearing.") {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            ResultLoader(state = state, color = Accent, size = 56.dp)
            ResultLoader(state = state, color = Color(0xFF22D3EE), size = 56.dp) {
                CometSpinner(color = Color(0xFF22D3EE), size = 56.dp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Load") { state = LoaderState.Loading }
            Chip("Succeed") { state = LoaderState.Success }
            Chip("Fail") { state = LoaderState.Error }
        }
    }
}

@Composable
private fun FlickerSection() {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    val smart by rememberLoadingVisibility(loading)
    fun request(millis: Long) {
        if (loading) return
        loading = true
        scope.launch { delay(millis); loading = false }
    }
    Section("rememberLoadingVisibility", "A fast request shouldn't flash a spinner. Compare both sides.") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FlickerBox("Plain if (loading)", loading)
            FlickerBox("rememberLoadingVisibility", smart)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Fast request (150 ms)") { request(150) }
            Chip("Slow request (1.5 s)") { request(1500) }
        }
        Spacer(Modifier.height(8.dp))
        Caption("Fast: the plain side flashes, the smart side shows nothing. Slow: both show it.")
    }
}

@Composable
private fun FlickerBox(title: String, show: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(140.dp, 80.dp).background(Background, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (show) CometSpinner(color = Accent, size = 36.dp)
        }
        Spacer(Modifier.height(6.dp))
        Caption(title)
    }
}

@Composable
private fun LogoSection() {
    var target by remember { mutableStateOf(0f) }
    val upload by animateFloatAsState(target, tween(if (target == 0f) 0 else 3000, easing = LinearEasing), label = "upload")
    Section("PathLoader", "Pass your logo (an ImageVector or SVG path) and get a loader in its shape.") {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Bottom) {
            LogoDemo("Trace") { PathLoader(DemoShapes.Badge, 24f, 24f, style = PathLoaderStyle.Trace, color = Accent) }
            LogoDemo("Fill") { PathLoader(DemoShapes.Cloud, 24f, 24f, style = PathLoaderStyle.Fill, color = Color(0xFF22D3EE)) }
            LogoDemo("Shimmer") { PathLoader(DemoShapes.Bolt, 24f, 24f, style = PathLoaderStyle.Shimmer, color = Color(0xFFFBBF24)) }
            LogoDemo("${(upload * 100).toInt()}%") {
                PathLoader(DemoShapes.Cloud, 24f, 24f, style = PathLoaderStyle.Fill, color = Color(0xFF34D399), progress = upload)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Upload") { target = if (target == 1f) 0f else 1f }
        }
        Spacer(Modifier.height(8.dp))
        Caption("With progress set, the logo fills to the real upload percentage.")
    }
}

@Composable
private fun LogoDemo(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        content()
        Spacer(Modifier.height(6.dp))
        Caption(label)
    }
}
