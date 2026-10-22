package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import java.lang.management.ManagementFactory
import kotlin.test.Test
import kotlin.test.fail

/**
 * Measures how many bytes each loader allocates per frame, on top of what rendering any animated
 * frame costs. The baseline is a dot that redraws every frame from the same kind of frame clock,
 * so the difference is the loader's own work: drawing objects, boxing, lambdas.
 *
 * Run with `./gradlew :gallery:desktopTest --tests '*AllocationTest*' -i` to see the table.
 */
class AllocationTest {
    // Loaders that rebuild more than a couple of paths every frame, and how many.
    private val RebuiltPathsPerFrame = mapOf("HeartbeatLine" to 1)

    private val threads = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
    private val warmup = 120
    private val measured = 600

    private fun bytesPerFrame(content: @androidx.compose.runtime.Composable () -> Unit): Double {
        var before = 0L
        var after = 0L
        renderFrames(warmup + measured, 60, content = content) { i, _, render ->
            if (i == warmup) before = threads.currentThreadAllocatedBytes
            render()
            if (i == warmup + measured - 1) after = threads.currentThreadAllocatedBytes
        }
        return (after - before).toDouble() / measured
    }

    @Test
    fun loadersAllocateNothingPerFrameBeyondTheRendererItself() {
        val baseline = bytesPerFrame { AnimatedDot() }
        val rows = LoaderCatalog.entries.map { entry ->
            entry.name to bytesPerFrame { entry.content(GalleryPalette.first()) } - baseline
        }
        println("Baseline (an animated dot): %.0f bytes per frame".format(baseline))
        for ((name, extra) in rows) println("%-16s %+8.0f bytes per frame".format(name, extra))
        // What the renderer itself costs for shapes loaders use, with no loader code involved.
        val roundRects = bytesPerFrame { Probe(roundRects = true) } - bytesPerFrame { Probe(roundRects = false) }
        println("Renderer cost of 9 drawRoundRect vs 9 drawCircle: %+.0f bytes per frame".format(roundRects))
        val rebuiltPath = bytesPerFrame { PathProbe(rebuild = true) } - bytesPerFrame { PathProbe(rebuild = false) }
        println("Renderer cost of drawing 1 path rebuilt each frame vs a fixed one: %+.0f bytes per frame".format(rebuiltPath))
        // Loader code itself allocates nothing per frame. What remains on desktop is Skiko: about
        // 24 bytes per drawRoundRect and about 200 bytes per path that is rebuilt and drawn in the
        // same frame (see the probes above). Android's Path and Canvas are native and have no such
        // wrappers. Rendering noise is about 100 bytes either way.
        val heavy = rows.filter { (name, extra) -> extra > 400.0 + 220.0 * (RebuiltPathsPerFrame[name] ?: 0) }
        if (heavy.isNotEmpty()) fail("Loaders allocating per frame: $heavy")
    }
}

@androidx.compose.runtime.Composable
private fun AnimatedDot() {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val onFrame: (Long) -> Unit = { t = (it % 1_000_000_000L) / 1e9f }
        while (true) withFrameNanos(onFrame)
    }
    Spacer(Modifier.size(48.dp).drawBehind { drawCircle(androidx.compose.ui.graphics.Color.White, 10f + t) })
}

@androidx.compose.runtime.Composable
private fun Probe(roundRects: Boolean) {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val onFrame: (Long) -> Unit = { t = (it % 1_000_000_000L) / 1e9f }
        while (true) withFrameNanos(onFrame)
    }
    Spacer(
        Modifier.size(48.dp).drawBehind {
            for (i in 0 until 9) {
                val at = androidx.compose.ui.geometry.Offset(i * 10f, t * 10f)
                if (roundRects) {
                    drawRoundRect(
                        androidx.compose.ui.graphics.Color.White, at,
                        androidx.compose.ui.geometry.Size(8f, 8f), androidx.compose.ui.geometry.CornerRadius(2f),
                    )
                } else {
                    drawCircle(androidx.compose.ui.graphics.Color.White, 4f, at)
                }
            }
        },
    )
}

@androidx.compose.runtime.Composable
private fun PathProbe(rebuild: Boolean) {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val onFrame: (Long) -> Unit = { t = (it % 1_000_000_000L) / 1e9f }
        while (true) withFrameNanos(onFrame)
    }
    val path = remember { androidx.compose.ui.graphics.Path().apply { moveTo(0f, 0f); lineTo(40f, 10f); lineTo(20f, 40f); close() } }
    Spacer(
        Modifier.size(48.dp).drawBehind {
            if (rebuild) {
                path.rewind()
                path.moveTo(0f, 0f)
                path.lineTo(40f, 10f + t)
                path.lineTo(20f, 40f)
                path.close()
            }
            drawPath(path, androidx.compose.ui.graphics.Color.White, alpha = 0.5f + t / 2f)
        },
    )
}
