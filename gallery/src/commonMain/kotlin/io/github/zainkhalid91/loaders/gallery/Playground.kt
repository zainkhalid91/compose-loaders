package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zainkhalid91.loaders.LoaderConfig
import io.github.zainkhalid91.loaders.ProvideLoaderDefaults
import io.github.zainkhalid91.loaders.ReducedMotion
import kotlin.math.roundToInt

/** Try any loader with live colour, speed, size, reduced-motion and pause controls. */
@Composable
internal fun Playground(modifier: Modifier = Modifier) {
    var selected by remember { mutableIntStateOf(1) }
    var tint by remember { mutableStateOf(GalleryPalette[1]) }
    var hue by remember { mutableFloatStateOf(-1f) }
    var speed by remember { mutableFloatStateOf(1f) }
    var scale by remember { mutableFloatStateOf(1.6f) }
    var motion by remember { mutableStateOf(ReducedMotion.Auto) }
    var running by remember { mutableStateOf(true) }
    val entry = LoaderCatalog.entries[selected]
    val color = if (hue >= 0f) Color.hsv(hue, 0.62f, 1f) else tint
    val config = LoaderConfig(speed = speed, isRunning = running, reducedMotion = motion, label = entry.name)

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Card, RoundedCornerShape(20.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.graphicsLayer(scaleX = scale, scaleY = scale)) {
                ProvideLoaderDefaults(config = config) { entry.content(color) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LoaderCatalog.entries.forEachIndexed { index, item ->
                Chip(item.name, index == selected) { selected = index }
            }
        }
        Section("Colour")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            for (swatch in GalleryPalette) {
                Box(
                    Modifier
                        .size(30.dp)
                        .border(2.dp, if (hue < 0f && swatch == tint) Color.White else Color.Transparent, CircleShape)
                        .padding(4.dp)
                        .background(swatch, CircleShape)
                        .clickable { tint = swatch; hue = -1f },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        HueSlider(if (hue < 0f) 0f else hue) { hue = it }
        Section("Speed  ${format(speed)}x")
        PlainSlider(speed, 0.25f..3f, "Speed") { speed = it }
        Section("Size  ${(scale * 48).roundToInt()} dp")
        PlainSlider(scale, 0.5f..3f, "Size") { scale = it }
        Section("Reduced motion")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (mode in ReducedMotion.entries) Chip(mode.name, mode == motion) { motion = mode }
            Spacer(Modifier.width(8.dp))
            Chip(if (running) "Pause" else "Resume", !running) { running = !running }
        }
        Section("Code")
        BasicText(
            snippet(entry.name, color, speed, motion, running),
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B0D11), RoundedCornerShape(12.dp))
                .padding(14.dp),
            style = TextStyle(color = Color(0xFFD7DBE6), fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp),
        )
    }
}

private fun snippet(name: String, color: Color, speed: Float, motion: ReducedMotion, running: Boolean): String {
    val options = buildList {
        if (speed != 1f) add("speed = ${format(speed)}f")
        if (motion != ReducedMotion.Auto) add("reducedMotion = ReducedMotion.${motion.name}")
        if (!running) add("isRunning = false")
    }
    val hex = (color.toArgb().toLong() and 0xFFFFFFFFL).toString(16).uppercase().padStart(8, '0')
    val config = if (options.isEmpty()) "" else ",\n    config = LoaderConfig(${options.joinToString()})"
    return "$name(\n    color = Color(0x$hex)$config,\n)"
}

private fun format(value: Float): String {
    val hundredths = (value * 100).roundToInt()
    val whole = hundredths / 100
    val rest = hundredths % 100
    return if (rest == 0) "$whole" else "$whole.${rest.toString().padStart(2, '0').trimEnd('0')}"
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(18.dp))
    BasicText(title, style = TextStyle(color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    BasicText(
        text,
        Modifier
            .background(if (selected) Color.White else Card, RoundedCornerShape(50))
            .border(1.dp, if (selected) Color.White else CardBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = TextStyle(color = if (selected) Background else Color.White, fontSize = 13.sp),
    )
}

/** A minimal slider built on foundation, so the gallery needs no Material dependency. */
@Composable
private fun PlainSlider(value: Float, range: ClosedFloatingPointRange<Float>, label: String, onChange: (Float) -> Unit) {
    SliderTrack(
        fraction = (value - range.start) / (range.endInclusive - range.start),
        label = label,
        state = format(value),
        track = Brush.horizontalGradient(listOf(CardBorder, CardBorder)),
    ) { onChange(range.start + it * (range.endInclusive - range.start)) }
}

@Composable
private fun HueSlider(hue: Float, onChange: (Float) -> Unit) {
    val rainbow = remember { Brush.horizontalGradient((0..6).map { Color.hsv(it * 60f % 360f, 0.62f, 1f) }) }
    SliderTrack(hue / 360f, "Hue", "${hue.roundToInt()} degrees", rainbow) { onChange(it * 359.9f) }
}

@Composable
private fun SliderTrack(fraction: Float, label: String, state: String, track: Brush, onFraction: (Float) -> Unit) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(28.dp)
            .semantics {
                contentDescription = label
                stateDescription = state
            }
            .pointerInput(Unit) {
                detectTapGestures { onFraction((it.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ -> onFraction((change.position.x / size.width).coerceIn(0f, 1f)) }
            },
    ) {
        val h = 6.dp.toPx()
        val y = size.height / 2f
        drawRoundRect(track, Offset(0f, y - h / 2f), androidx.compose.ui.geometry.Size(size.width, h), CornerRadius(h / 2f))
        val x = fraction.coerceIn(0f, 1f) * size.width
        drawCircle(Color.White, 10.dp.toPx(), Offset(x.coerceIn(10.dp.toPx(), size.width - 10.dp.toPx()), y))
    }
}
