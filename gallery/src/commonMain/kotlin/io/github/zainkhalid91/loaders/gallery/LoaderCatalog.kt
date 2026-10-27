package io.github.zainkhalid91.loaders.gallery

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zainkhalid91.loaders.AtomOrbit
import io.github.zainkhalid91.loaders.BatteryCharge
import io.github.zainkhalid91.loaders.BouncingBall
import io.github.zainkhalid91.loaders.BouncingDots
import io.github.zainkhalid91.loaders.BreathingCircle
import io.github.zainkhalid91.loaders.ChasingBlocks
import io.github.zainkhalid91.loaders.ClockSpinner
import io.github.zainkhalid91.loaders.CometSpinner
import io.github.zainkhalid91.loaders.DnaHelix
import io.github.zainkhalid91.loaders.DominoFall
import io.github.zainkhalid91.loaders.DualRing
import io.github.zainkhalid91.loaders.ExpressiveArc
import io.github.zainkhalid91.loaders.FireworkBurst
import io.github.zainkhalid91.loaders.FlippingSquare
import io.github.zainkhalid91.loaders.FoldingCube
import io.github.zainkhalid91.loaders.GearPair
import io.github.zainkhalid91.loaders.GooeyOrbit
import io.github.zainkhalid91.loaders.GridPulse
import io.github.zainkhalid91.loaders.HeartPulse
import io.github.zainkhalid91.loaders.HeartbeatLine
import io.github.zainkhalid91.loaders.Hourglass
import io.github.zainkhalid91.loaders.InfinityTrail
import io.github.zainkhalid91.loaders.LiquidFill
import io.github.zainkhalid91.loaders.MorphingPolygon
import io.github.zainkhalid91.loaders.NeonTrace
import io.github.zainkhalid91.loaders.NewtonCradle
import io.github.zainkhalid91.loaders.OrbitingMoons
import io.github.zainkhalid91.loaders.PacDots
import io.github.zainkhalid91.loaders.Pendulum
import io.github.zainkhalid91.loaders.PulseRipple
import io.github.zainkhalid91.loaders.RadarSweep
import io.github.zainkhalid91.loaders.Rocket
import io.github.zainkhalid91.loaders.SegmentedRing
import io.github.zainkhalid91.loaders.SignalBars
import io.github.zainkhalid91.loaders.SkeletonShimmer
import io.github.zainkhalid91.loaders.SlidingBar
import io.github.zainkhalid91.loaders.SlidingTiles
import io.github.zainkhalid91.loaders.SpiralGalaxy
import io.github.zainkhalid91.loaders.SpokeSpinner
import io.github.zainkhalid91.loaders.SpringCoil
import io.github.zainkhalid91.loaders.StripedBar
import io.github.zainkhalid91.loaders.TypingBubble
import io.github.zainkhalid91.loaders.WaveBars
import io.github.zainkhalid91.loaders.WaveText
import io.github.zainkhalid91.loaders.YinYang

public enum class LoaderCategory { Spinner, Dots, Bars, Shapes, Path, Physics, Fill, Text, Placeholder }

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
        LoaderEntry(26, "TypingBubble", LoaderCategory.Dots, "2026-W45") { TypingBubble(color = it) },
        LoaderEntry(27, "WaveText", LoaderCategory.Text, "2026-W45") { WaveText(color = it) },
        LoaderEntry(28, "OrbitingMoons", LoaderCategory.Path, "2026-W45") { OrbitingMoons(color = it) },
        LoaderEntry(29, "FoldingCube", LoaderCategory.Shapes, "2026-W45") { FoldingCube(color = it) },
        LoaderEntry(30, "SegmentedRing", LoaderCategory.Spinner, "2026-W45") { SegmentedRing(color = it) },
        LoaderEntry(31, "SkeletonShimmer", LoaderCategory.Placeholder, "2026-W45") { SkeletonShimmer(color = it, width = 120.dp, height = 40.dp) },
        LoaderEntry(32, "SpiralGalaxy", LoaderCategory.Path, "2026-W45") { SpiralGalaxy(color = it) },
        LoaderEntry(33, "Pendulum", LoaderCategory.Physics, "2026-W45") { Pendulum(color = it) },
        LoaderEntry(34, "SlidingTiles", LoaderCategory.Shapes, "2026-W45") { SlidingTiles(color = it) },
        LoaderEntry(35, "BreathingCircle", LoaderCategory.Shapes, "2026-W45") { BreathingCircle(color = it) },
        LoaderEntry(36, "DominoFall", LoaderCategory.Physics, "2026-W45") { DominoFall(color = it) },
        LoaderEntry(37, "NeonTrace", LoaderCategory.Path, "2026-W45") { NeonTrace(color = it) },
        LoaderEntry(38, "BatteryCharge", LoaderCategory.Fill, "2026-W45") { BatteryCharge(color = it) },
        LoaderEntry(39, "Rocket", LoaderCategory.Physics, "2026-W45") { Rocket(color = it) },
        LoaderEntry(40, "SignalBars", LoaderCategory.Bars, "2026-W45") { SignalBars(color = it) },
        LoaderEntry(41, "GearPair", LoaderCategory.Spinner, "2026-W45") { GearPair(color = it) },
        LoaderEntry(42, "YinYang", LoaderCategory.Spinner, "2026-W45") { YinYang(color = it) },
        LoaderEntry(43, "SpringCoil", LoaderCategory.Physics, "2026-W45") { SpringCoil(color = it) },
        LoaderEntry(44, "GooeyOrbit", LoaderCategory.Shapes, "2026-W45") { GooeyOrbit(color = it) },
        LoaderEntry(45, "HeartPulse", LoaderCategory.Shapes, "2026-W45") { HeartPulse(color = it) },
    )
}
