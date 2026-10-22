package io.github.zainkhalid91.loaders.gallery

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zainkhalid91.loaders.AtomOrbit
import io.github.zainkhalid91.loaders.BouncingBall
import io.github.zainkhalid91.loaders.BouncingDots
import io.github.zainkhalid91.loaders.ChasingBlocks
import io.github.zainkhalid91.loaders.ClockSpinner
import io.github.zainkhalid91.loaders.CometSpinner
import io.github.zainkhalid91.loaders.DnaHelix
import io.github.zainkhalid91.loaders.DualRing
import io.github.zainkhalid91.loaders.ExpressiveArc
import io.github.zainkhalid91.loaders.FireworkBurst
import io.github.zainkhalid91.loaders.FlippingSquare
import io.github.zainkhalid91.loaders.GridPulse
import io.github.zainkhalid91.loaders.HeartbeatLine
import io.github.zainkhalid91.loaders.Hourglass
import io.github.zainkhalid91.loaders.InfinityTrail
import io.github.zainkhalid91.loaders.LiquidFill
import io.github.zainkhalid91.loaders.MorphingPolygon
import io.github.zainkhalid91.loaders.NewtonCradle
import io.github.zainkhalid91.loaders.PacDots
import io.github.zainkhalid91.loaders.PulseRipple
import io.github.zainkhalid91.loaders.RadarSweep
import io.github.zainkhalid91.loaders.SlidingBar
import io.github.zainkhalid91.loaders.SpokeSpinner
import io.github.zainkhalid91.loaders.StripedBar
import io.github.zainkhalid91.loaders.WaveBars

public enum class LoaderCategory { Spinner, Dots, Bars, Shapes, Path, Physics, Fill }

/**
 * One loader in the gallery. [id] is permanent: it is also the row number in LOADERS.md,
 * which a test keeps in sync with this catalog.
 */
public class LoaderEntry(
    public val id: Int,
    public val name: String,
    public val category: LoaderCategory,
    public val batch: String,
    public val content: @Composable (Color) -> Unit,
)

public object LoaderCatalog {
    public val entries: List<LoaderEntry> = listOf(
        LoaderEntry(1, "SpokeSpinner", LoaderCategory.Spinner, "2026-W43") { SpokeSpinner(color = it) },
        LoaderEntry(2, "ExpressiveArc", LoaderCategory.Spinner, "2026-W43") { ExpressiveArc(color = it) },
        LoaderEntry(3, "CometSpinner", LoaderCategory.Spinner, "2026-W43") { CometSpinner(color = it) },
        LoaderEntry(4, "DualRing", LoaderCategory.Spinner, "2026-W43") { DualRing(color = it) },
        LoaderEntry(5, "PulseRipple", LoaderCategory.Shapes, "2026-W43") { PulseRipple(color = it) },
        LoaderEntry(6, "BouncingDots", LoaderCategory.Dots, "2026-W43") { BouncingDots(color = it) },
        LoaderEntry(7, "WaveBars", LoaderCategory.Bars, "2026-W43") { WaveBars(color = it) },
        LoaderEntry(8, "GridPulse", LoaderCategory.Shapes, "2026-W43") { GridPulse(color = it) },
        LoaderEntry(9, "FlippingSquare", LoaderCategory.Shapes, "2026-W43") { FlippingSquare(color = it) },
        LoaderEntry(10, "ChasingBlocks", LoaderCategory.Shapes, "2026-W43") { ChasingBlocks(color = it) },
        LoaderEntry(11, "NewtonCradle", LoaderCategory.Physics, "2026-W43") { NewtonCradle(color = it) },
        LoaderEntry(12, "InfinityTrail", LoaderCategory.Path, "2026-W43") { InfinityTrail(color = it) },
        LoaderEntry(13, "DnaHelix", LoaderCategory.Dots, "2026-W43") { DnaHelix(color = it) },
        LoaderEntry(14, "AtomOrbit", LoaderCategory.Path, "2026-W43") { AtomOrbit(color = it) },
        LoaderEntry(15, "HeartbeatLine", LoaderCategory.Path, "2026-W43") { HeartbeatLine(color = it) },
        LoaderEntry(16, "MorphingPolygon", LoaderCategory.Shapes, "2026-W43") { MorphingPolygon(color = it) },
        LoaderEntry(17, "LiquidFill", LoaderCategory.Fill, "2026-W43") { LiquidFill(color = it) },
        LoaderEntry(18, "ClockSpinner", LoaderCategory.Spinner, "2026-W43") { ClockSpinner(color = it) },
        LoaderEntry(19, "BouncingBall", LoaderCategory.Physics, "2026-W43") { BouncingBall(color = it) },
        LoaderEntry(20, "StripedBar", LoaderCategory.Bars, "2026-W43") { StripedBar(color = it, width = 110.dp) },
        LoaderEntry(21, "RadarSweep", LoaderCategory.Spinner, "2026-W44") { RadarSweep(color = it) },
        LoaderEntry(22, "Hourglass", LoaderCategory.Physics, "2026-W44") { Hourglass(color = it) },
        LoaderEntry(23, "PacDots", LoaderCategory.Dots, "2026-W44") { PacDots(color = it) },
        LoaderEntry(24, "SlidingBar", LoaderCategory.Bars, "2026-W44") { SlidingBar(color = it, width = 110.dp) },
        LoaderEntry(25, "FireworkBurst", LoaderCategory.Shapes, "2026-W44") { FireworkBurst(color = it) },
    )
}
