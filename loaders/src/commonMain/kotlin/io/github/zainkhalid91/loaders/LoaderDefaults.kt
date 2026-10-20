package io.github.zainkhalid91.loaders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Default values shared by every loader in the library. */
public object LoaderDefaults {
    /** Default tint. Every loader takes a `color` parameter, so this is only a fallback. */
    public val color: Color = Color(0xFF7C5CFF)

    /** Default edge length for square loaders. Wide loaders document their own sizes. */
    public val size: Dp = 48.dp
}
