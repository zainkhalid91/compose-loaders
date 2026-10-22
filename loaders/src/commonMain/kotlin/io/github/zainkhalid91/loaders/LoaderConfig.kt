package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** What a loader does when the user has asked for less motion. */
public enum class ReducedMotion {
    /**
     * Follow the platform. On Android that is the system animator duration scale: "Remove animations"
     * (scale 0) switches to reduced motion. Compose Desktop has no such signal, so Auto means full motion
     * there unless the app provides a [androidx.compose.ui.MotionDurationScale] of 0.
     */
    Auto,

    /** Always reduced: the loader holds still and slowly pulses its opacity, so it still reads as busy. */
    On,

    /** Always full motion, even when the system scale is 0. A loader never freezes. */
    Off,
}

/**
 * Behaviour shared by every loader. It is immutable, so a loader only recomposes when the config
 * itself changes; per-frame drawing never reads it.
 *
 * Set it once for the whole app with [ProvideLoaderDefaults], or pass one to a single loader.
 *
 * @param speed Multiplies the loader's own speed. 2f runs twice as fast, 0.5f half as fast.
 * @param isRunning When false the loader stops on its current frame.
 * @param reducedMotion See [ReducedMotion].
 * @param label What a screen reader says, such as "Loading photos". Pass a translated string.
 * @param announce When true the loader is a polite live region: a screen reader announces [label]
 *   once when the loader appears, and again if the label or progress changes. Turn it off when
 *   many loaders are on screen at once.
 */
@Immutable
public data class LoaderConfig(
    val speed: Float = 1f,
    val isRunning: Boolean = true,
    val reducedMotion: ReducedMotion = ReducedMotion.Auto,
    val label: String = LoaderDefaults.label,
    val announce: Boolean = true,
) {
    init {
        require(speed > 0f) { "speed must be positive, was $speed" }
    }
}

/** App-wide [LoaderConfig], read by every loader's `config` parameter. */
public val LocalLoaderConfig: ProvidableCompositionLocal<LoaderConfig> = staticCompositionLocalOf { LoaderConfig() }

/** App-wide loader colour, read by every loader's `color` parameter. */
public val LocalLoaderColor: ProvidableCompositionLocal<Color> = compositionLocalOf { LoaderDefaults.color }

/**
 * Sets the colour and behaviour of every loader in [content], so a team sets its brand once:
 *
 * ```
 * ProvideLoaderDefaults(color = MaterialTheme.colorScheme.primary, config = LoaderConfig(speed = 0.8f)) {
 *     AppContent()
 * }
 * ```
 */
@Composable
public fun ProvideLoaderDefaults(
    color: Color = LocalLoaderColor.current,
    config: LoaderConfig = LocalLoaderConfig.current,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLoaderColor provides color, LocalLoaderConfig provides config, content = content)
}
