package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathIterator
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** How [PathLoader] animates your shape. */
public enum class PathLoaderStyle {
    /** A pen draws the outline, holds it for a moment, then erases it from where it started. */
    Trace,

    /** The shape fills from the bottom like a glass of liquid, then drains. */
    Fill,

    /** The filled shape is a placeholder with a highlight sweeping across it. */
    Shimmer,
}

/**
 * Turns any shape, such as your app's logo, into a loader. Pass an [ImageVector]: a Material
 * icon, or your logo imported in Android Studio (New > Vector Asset) or as a Compose ImageVector.
 *
 * ```
 * PathLoader(imageVector = Icons.Rounded.Favorite, style = PathLoaderStyle.Fill)
 * ```
 *
 * With [progress] set, the loader shows real progress instead of looping: [PathLoaderStyle.Trace]
 * draws that share of the outline and [PathLoaderStyle.Fill] fills to that level, so an upload
 * can fill your logo. Shapes with several parts and holes (letters such as "O") work.
 *
 * @param trackColor The faint outline (Trace) or empty shape (Fill, Shimmer) under the animation.
 */
@Composable
public fun PathLoader(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    style: PathLoaderStyle = PathLoaderStyle.Trace,
    color: Color = LocalLoaderColor.current,
    trackColor: Color = color.copy(alpha = 0.18f),
    size: Dp = 64.dp,
    durationMillis: Int = 2400,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    val shape = remember(imageVector) { LoaderShape.from(imageVector) }
    PathLoaderContent(shape, modifier, style, color, trackColor, size, durationMillis, strokeWidth, progress, config)
}

/**
 * [PathLoader] from SVG path data, the `d` attribute of a `<path>`, so you can paste your logo
 * straight from an SVG file. [viewportWidth] and [viewportHeight] are the SVG's `viewBox` size.
 */
@Composable
public fun PathLoader(
    pathData: String,
    viewportWidth: Float,
    viewportHeight: Float,
    modifier: Modifier = Modifier,
    style: PathLoaderStyle = PathLoaderStyle.Trace,
    color: Color = LocalLoaderColor.current,
    trackColor: Color = color.copy(alpha = 0.18f),
    size: Dp = 64.dp,
    durationMillis: Int = 2400,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    val shape = remember(pathData, viewportWidth, viewportHeight) {
        LoaderShape.from(pathData, viewportWidth, viewportHeight)
    }
    PathLoaderContent(shape, modifier, style, color, trackColor, size, durationMillis, strokeWidth, progress, config)
}

@Composable
private fun PathLoaderContent(
    shape: LoaderShape,
    modifier: Modifier,
    style: PathLoaderStyle,
    color: Color,
    trackColor: Color,
    size: Dp,
    durationMillis: Int,
    strokeWidth: Dp,
    progress: Float?,
    config: LoaderConfig,
) {
    val restPose = when (style) {
        PathLoaderStyle.Trace -> 0.55f
        PathLoaderStyle.Fill -> 0.5f
        PathLoaderStyle.Shimmer -> 0.45f
    }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose) { motion ->
        val stroke = strokePx(strokeWidth, this.size.minDimension * 0.045f)
        val fitted = shape.fitted(this.size, inset = stroke / 2f + 1f)
        val strokeStyle = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val bounds = Rect(Offset.Zero, this.size)
        val layerPaint = Paint()
        // Composites the shape mask with DstIn, so only what lies inside the shape survives.
        val maskPaint = Paint().apply { blendMode = BlendMode.DstIn }
        when (style) {
            PathLoaderStyle.Trace -> {
                val pieces = fitted.contours.map { PathMeasure().apply { setPath(it, false) } }
                val lengths = FloatArray(pieces.size) { pieces[it].length }
                val total = lengths.sum()
                val segments = List(pieces.size) { Path() }
                onDrawBehind {
                    for (c in fitted.contours) drawPath(c, trackColor, style = strokeStyle)
                    val p = motion.progress
                    val head: Float
                    val tail: Float
                    if (progress != null) {
                        head = progress.coerceIn(0f, 1f) * total
                        tail = 0f
                    } else {
                        head = FastOutSlowInEasing.transform(smoothstep(0f, 0.5f, p)) * total
                        tail = FastOutSlowInEasing.transform(smoothstep(0.62f, 1f, p)) * total
                        // A soft fill while the whole outline is drawn.
                        val glow = smoothstep(0.42f, 0.55f, p) * (1f - smoothstep(0.6f, 0.75f, p))
                        if (glow > 0f) for (f in fitted.fills) drawPath(f, color.copy(alpha = color.alpha * 0.22f * glow))
                    }
                    var start = 0f
                    for (i in pieces.indices) {
                        val end = start + lengths[i]
                        val a = maxOf(tail, start)
                        val b = min(head, end)
                        if (b > a) {
                            val segment = segments[i]
                            segment.rewind()
                            pieces[i].getSegment(a - start, b - start, segment, true)
                            drawPath(segment, color, style = strokeStyle)
                        }
                        start = end
                    }
                }
            }
            PathLoaderStyle.Fill -> {
                val top = fitted.bounds.top
                val height = fitted.bounds.height
                val wave = Path()
                val amplitude = this.size.minDimension * 0.035f
                onDrawBehind {
                    val p = motion.progress
                    val level = if (progress != null) {
                        progress.coerceIn(0f, 1f)
                    } else {
                        FastOutSlowInEasing.transform(smoothstep(0f, 0.72f, p)) * (1f - smoothstep(0.8f, 1f, p))
                    }
                    for (f in fitted.fills) drawPath(f, trackColor)
                    if (level > 0f) {
                        drawIntoCanvas { it.saveLayer(bounds, layerPaint) }
                        // Overfill a little so a full shape has no wave showing at the top.
                        val surface = top + height * (1f - level) - if (level >= 1f) amplitude * 2f else 0f
                        val w = this.size.width
                        val phase = p * TwoPi * 3f
                        wave.rewind()
                        wave.moveTo(0f, this.size.height)
                        wave.lineTo(0f, surface)
                        val steps = 24
                        for (s in 1..steps) {
                            val x = w * s / steps
                            wave.lineTo(x, surface + amplitude * sin(phase + x / w * TwoPi * 1.5f))
                        }
                        wave.lineTo(w, this.size.height)
                        wave.close()
                        drawPath(wave, color)
                        drawIntoCanvas { it.saveLayer(bounds, maskPaint) }
                        for (f in fitted.fills) drawPath(f, Color.Black)
                        drawIntoCanvas { it.restore(); it.restore() }
                    }
                }
            }
            PathLoaderStyle.Shimmer -> {
                val band = this.size.width * 0.55f
                val highlight = Brush.linearGradient(
                    0f to Color.Transparent,
                    0.5f to color,
                    1f to Color.Transparent,
                    start = Offset(0f, 0f),
                    end = Offset(band, this.size.height * 0.35f),
                )
                val bandSize = Size(band, this.size.height)
                onDrawBehind {
                    val x = -band + (this.size.width + band) * motion.progress
                    for (f in fitted.fills) drawPath(f, trackColor)
                    drawIntoCanvas { it.saveLayer(bounds, layerPaint) }
                    translate(left = x) { drawRect(highlight, size = bandSize) }
                    drawIntoCanvas { it.saveLayer(bounds, maskPaint) }
                    for (f in fitted.fills) drawPath(f, Color.Black)
                    drawIntoCanvas { it.restore(); it.restore() }
                }
            }
        }
    }
}

/** A shape in its own coordinates: fill paths (with their fill rules) and outline contours. */
@Immutable
internal class LoaderShape(val fills: List<Path>, val contours: List<Path>, val bounds: Rect) {
    /** Copies of the paths scaled and centred to fit [size], keeping the aspect ratio. */
    fun fitted(size: Size, inset: Float): LoaderShape {
        val w = (size.width - inset * 2f).coerceAtLeast(1f)
        val h = (size.height - inset * 2f).coerceAtLeast(1f)
        val scale = min(w / bounds.width.coerceAtLeast(1e-3f), h / bounds.height.coerceAtLeast(1e-3f))
        val dx = (size.width - bounds.width * scale) / 2f - bounds.left * scale
        val dy = (size.height - bounds.height * scale) / 2f - bounds.top * scale
        val m = Matrix()
        m.values[Matrix.ScaleX] = scale
        m.values[Matrix.ScaleY] = scale
        m.values[Matrix.TranslateX] = dx
        m.values[Matrix.TranslateY] = dy
        fun Path.mapped() = Path().also { it.addPath(this); it.fillType = fillType; it.transform(m) }
        val fitBounds = Rect(
            bounds.left * scale + dx, bounds.top * scale + dy,
            bounds.right * scale + dx, bounds.bottom * scale + dy,
        )
        return LoaderShape(fills.map { it.mapped() }, contours.map { it.mapped() }, fitBounds)
    }

    companion object {
        fun from(pathData: String, viewportWidth: Float, viewportHeight: Float): LoaderShape {
            val path = PathParser().parsePathString(pathData).toPath()
            return of(listOf(path), Rect(0f, 0f, viewportWidth, viewportHeight))
        }

        fun from(vector: ImageVector): LoaderShape {
            val paths = mutableListOf<Path>()
            collect(vector.root, Affine.Identity, paths)
            return of(paths, Rect(0f, 0f, vector.viewportWidth, vector.viewportHeight))
        }

        private fun of(paths: List<Path>, viewport: Rect): LoaderShape {
            val contours = paths.flatMap { splitContours(it) }
            require(contours.isNotEmpty()) { "PathLoader needs a shape with at least one path" }
            // Fit the drawn shape, not the viewport, so icons with padding still fill the loader.
            val bounds = paths.map { it.getBounds() }.reduce { a, b ->
                Rect(min(a.left, b.left), min(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom))
            }
            return LoaderShape(paths, contours, if (bounds.isEmpty) viewport else bounds)
        }

        private fun collect(group: VectorGroup, parent: Affine, out: MutableList<Path>) {
            val local = parent * Affine.group(
                group.translationX, group.translationY, group.pivotX, group.pivotY,
                group.rotation, group.scaleX, group.scaleY,
            )
            for (node in group) {
                when (node) {
                    is VectorGroup -> collect(node, local, out)
                    is VectorPath -> {
                        val path = node.pathData.toPath()
                        path.fillType = node.pathFillType
                        if (!local.isIdentity) path.transform(local.toMatrix())
                        out += path
                    }
                }
            }
        }
    }
}

/** Splits a path at every move, so each outline can be measured on its own. */
internal fun splitContours(path: Path): List<Path> {
    val out = mutableListOf<Path>()
    var current: Path? = null
    val iterator = path.iterator(PathIterator.ConicEvaluation.AsQuadratics)
    while (iterator.hasNext()) {
        val segment = iterator.next()
        val p = segment.points
        when (segment.type) {
            PathSegment.Type.Move -> current = Path().also { out += it; it.moveTo(p[0], p[1]) }
            PathSegment.Type.Line -> current?.lineTo(p[2], p[3])
            PathSegment.Type.Quadratic -> current?.quadraticTo(p[2], p[3], p[4], p[5])
            PathSegment.Type.Cubic -> current?.cubicTo(p[2], p[3], p[4], p[5], p[6], p[7])
            PathSegment.Type.Close -> current?.close()
            else -> Unit
        }
    }
    return out.filter { PathMeasure().apply { setPath(it, false) }.length > 0f }
        .onEach { it.fillType = PathFillType.NonZero }
}

/** A 2D affine transform (a c e / b d f), enough for vector group transforms. */
internal class Affine(val a: Float, val b: Float, val c: Float, val d: Float, val e: Float, val f: Float) {
    val isIdentity: Boolean get() = a == 1f && b == 0f && c == 0f && d == 1f && e == 0f && f == 0f

    operator fun times(o: Affine): Affine = Affine(
        a * o.a + c * o.b, b * o.a + d * o.b,
        a * o.c + c * o.d, b * o.c + d * o.d,
        a * o.e + c * o.f + e, b * o.e + d * o.f + f,
    )

    fun toMatrix(): Matrix = Matrix().also {
        it.values[Matrix.ScaleX] = a
        it.values[Matrix.SkewY] = b
        it.values[Matrix.SkewX] = c
        it.values[Matrix.ScaleY] = d
        it.values[Matrix.TranslateX] = e
        it.values[Matrix.TranslateY] = f
    }

    companion object {
        val Identity: Affine = Affine(1f, 0f, 0f, 1f, 0f, 0f)

        /** Same order as Android's VectorDrawable: scale and rotate about the pivot, then translate. */
        fun group(tx: Float, ty: Float, px: Float, py: Float, degrees: Float, sx: Float, sy: Float): Affine {
            val r = degrees * Pi / 180f
            val cs = cos(r)
            val sn = sin(r)
            val toPivot = Affine(1f, 0f, 0f, 1f, -px, -py)
            val scale = Affine(sx, 0f, 0f, sy, 0f, 0f)
            val rotate = Affine(cs, sn, -sn, cs, 0f, 0f)
            val back = Affine(1f, 0f, 0f, 1f, px + tx, py + ty)
            return back * rotate * scale * toPivot
        }
    }
}
