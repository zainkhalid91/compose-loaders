package io.github.zainkhalid91.loaders.gallery

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

/**
 * An offscreen scene whose coroutine delays run on the same virtual clock as its frames, so
 * timing inside the library (rememberLoadingVisibility, a button's result hold) matches the video
 * exactly instead of depending on how fast the machine renders.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalCoroutinesApi::class)
internal class VirtualScene(width: Int, height: Int, density: Float, content: @Composable () -> Unit) : AutoCloseable {
    private val scheduler = TestCoroutineScheduler()
    private val scene = ImageComposeScene(width, height, Density(density), UnconfinedTestDispatcher(scheduler), content)
    private var nowMillis = 0L

    fun render(nanos: Long): Image {
        val millis = nanos / 1_000_000L
        if (millis > nowMillis) {
            scheduler.advanceTimeBy(millis - nowMillis)
            nowMillis = millis
        }
        scheduler.runCurrent()
        return scene.render(nanos)
    }

    override fun close() = scene.close()
}

internal fun Image.writePng(file: File) = file.writeBytes(encodeToData(EncodedImageFormat.PNG)!!.bytes)
