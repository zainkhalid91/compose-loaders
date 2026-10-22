package io.github.zainkhalid91.loaders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Default values shared by every loader in the library. */
public object LoaderDefaults {
    /**
     * Default tint. It has at least 4.3:1 contrast on both white and black, above the 3:1 that
     * WCAG asks of graphics. Apps usually replace it with [ProvideLoaderDefaults].
     */
    public val color: Color = Color(0xFF7C5CFF)

    /** Default edge length for square loaders. Wide loaders document their own sizes. */
    public val size: Dp = 48.dp

    /** Default screen reader label. Pass a translated one through [LoaderConfig.label]. */
    public const val label: String = "Loading"
}
