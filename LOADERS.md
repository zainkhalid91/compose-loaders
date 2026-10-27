# Loader registry

Every loader in the library, in the order it was added. This file is the source of truth
for "have we done this already?" and a test (`gallery/src/desktopTest/.../RegistryTest.kt`)
fails the build if it drifts from `LoaderCatalog` or from the public functions in `:loaders`.

## Rules for adding a loader

1. Read the **Motion signature** column first. A new loader must differ in its *motion idea*,
   not just its colour, count, size or speed. "BouncingDots with 5 dots" is a parameter, not a loader.
2. If an idea in the backlog below is close to an existing row, merge it into that loader as a
   parameter instead.
3. Add the next number. Numbers are permanent; never reuse one, even if a loader is removed
   (mark it ~~struck through~~ instead).
4. Add the row here, the entry in `LoaderCatalog`, and the file in `loaders/src/commonMain`.
   `./gradlew :gallery:desktopTest` checks all three agree.

## Shipped

| # | Name | Category | Motion signature | Batch |
|---|------|----------|------------------|-------|
| 1 | `SpokeSpinner` | Spinner | 12 rounded spokes, opacity steps clockwise (system spinner) | 2026-W43 |
| 2 | `ExpressiveArc` | Spinner | one arc, head races ahead then tail catches up, ring keeps turning | 2026-W43 |
| 3 | `CometSpinner` | Spinner | rotating arc with sweep-gradient tail fading to transparent, bright head dot | 2026-W43 |
| 4 | `DualRing` | Spinner | two arc pairs on nested rings, counter-rotating at 1x and 1.5x | 2026-W43 |
| 5 | `PulseRipple` | Shapes | rings expand from a beating core and fade (sonar) | 2026-W43 |
| 6 | `BouncingDots` | Dots | dots hop in sequence and rest on a baseline | 2026-W43 |
| 7 | `WaveBars` | Bars | equalizer bars stretch from the centre line in a travelling wave | 2026-W43 |
| 8 | `GridPulse` | Shapes | 3x3 tiles shrink and return in a diagonal sweep | 2026-W43 |
| 9 | `FlippingSquare` | Shapes | single tile flips in 3D over X, then over Y | 2026-W43 |
| 10 | `ChasingBlocks` | Shapes | two blocks travel the corners of a square, tumbling and shrinking mid-move | 2026-W43 |
| 11 | `NewtonCradle` | Physics | five hanging balls, outer ones swing alternately | 2026-W43 |
| 12 | `InfinityTrail` | Path | dot with fading trail on a lemniscate (figure-eight) | 2026-W43 |
| 13 | `DnaHelix` | Dots | two bead strands twisting, depth by size and fade | 2026-W43 |
| 14 | `AtomOrbit` | Path | electrons on three tilted ellipses around a pulsing nucleus | 2026-W43 |
| 15 | `HeartbeatLine` | Path | pulse runs along an ECG trace with a gradient tail | 2026-W43 |
| 16 | `MorphingPolygon` | Shapes | filled shape morphs triangle, square, hexagon (via a circle each time) while turning | 2026-W43 |
| 17 | `LiquidFill` | Fill | two wave layers rise and fall inside a round flask | 2026-W43 |
| 18 | `ClockSpinner` | Spinner | clock hands race; hour hand turns once per 12 minute turns | 2026-W43 |
| 19 | `BouncingBall` | Physics | gravity bounce with squash on impact and a growing shadow | 2026-W43 |
| 20 | `StripedBar` | Bars | pill bar with diagonal candy stripes scrolling (barber pole) | 2026-W43 |
| 21 | `RadarSweep` | Spinner | filled gradient beam sweeps a scope; fixed blips light as it passes, then fade | 2026-W44 |
| 22 | `Hourglass` | Physics | sand drains top to bottom through a neck, then the glass flips 180 degrees | 2026-W44 |
| 23 | `PacDots` | Dots | chomping mouth eats a row of dots that slides in one slot per loop | 2026-W44 |
| 24 | `SlidingBar` | Bars | linear indeterminate: one segment stretches in, crosses the track, shrinks out | 2026-W44 |
| 25 | `FireworkBurst` | Shapes | two staggered rings of sparks fly out, droop under gravity and fade | 2026-W44 |
| 26 | `TypingBubble` | Dots | chat bubble breathes while its dots brighten and lift in turn (someone typing) | 2026-W45 |
| 27 | `WaveText` | Text | letters of a word hop one after another and brighten as they rise | 2026-W45 |
| 28 | `OrbitingMoons` | Path | moons on tilted orbits at different speeds pass behind a planet, shrinking with distance | 2026-W45 |
| 29 | `FoldingCube` | Shapes | four quarters of a diamond fold in one by one around the centre, hold, fold away | 2026-W45 |
| 30 | `SegmentedRing` | Spinner | separate arc segments fill one by one round a dial, then empty in the same order | 2026-W45 |
| 31 | `SkeletonShimmer` | Placeholder | placeholder avatar and text lines with a diagonal highlight sweeping across | 2026-W45 |
| 32 | `SpiralGalaxy` | Path | stars drift inward along turning spiral arms, fading into a glowing core | 2026-W45 |
| 33 | `Pendulum` | Physics | single pendulum swing with fading afterimages, fastest at the bottom | 2026-W45 |
| 34 | `SlidingTiles` | Shapes | three tiles take turns sliding into the empty corner of a 2x2 puzzle | 2026-W45 |
| 35 | `BreathingCircle` | Shapes | layered discs breathe in, hold, out, hold; outer layers lag behind (meditation) | 2026-W45 |
| 36 | `DominoFall` | Physics | row of dominoes topples one into the next, last falls flat, row resets | 2026-W45 |
| 37 | `NeonTrace` | Path | glowing pen traces a rounded outline, flares to full glow, then wipes away | 2026-W45 |
| 38 | `BatteryCharge` | Fill | battery cells light one by one from empty to full, hold, then drain | 2026-W45 |
| 39 | `Rocket` | Physics | rocket bobs and sways with a particle exhaust while stars stream past | 2026-W45 |
| 40 | `SignalBars` | Bars | bottom-aligned staircase bars; strength wanders up and down like a live signal | 2026-W45 |
| 41 | `GearPair` | Spinner | two meshing gears counter-rotate at the ratio of their teeth | 2026-W45 |
| 42 | `YinYang` | Spinner | yin-yang turns half a revolution with easing, pauses, turns again | 2026-W45 |
| 43 | `SpringCoil` | Physics | weight bounces on a hanging spring whose coils bunch and spread | 2026-W45 |
| 44 | `GooeyOrbit` | Shapes | blobs orbit a centre drop and stretch from it with liquid metaball bridges | 2026-W45 |
| 45 | `HeartPulse` | Shapes | heart beats a lub-dub double beat and sends out a fading outline echo | 2026-W45 |

## Backlog (ideas not yet built)

Pick from here for the weekly batch, then move the row up into **Shipped**.

- Wifi arcs: arcs above a dot light outward in turn
- Snake: a segmented snake winds round a small grid
- Ink drop: a drop falls into liquid and sends out a crown splash
- Orbiting squares: squares orbiting a centre, each spinning on its own axis
- Paper plane: a plane loops along a dotted path
- Eclipse: a dark disc slides across a glowing one and out again
- Tetris: falling blocks stack and clear a line
- Equaliser circle: radial bars round a ring pulsing like audio

