package io.github.zainkhalid91.loaders

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * A button that turns into its own loader. Tap it and the label fades, the button shrinks into a
 * circle with a loader inside, then it fills green with a tick (or red with a cross and a shake)
 * and grows back:
 *
 * ```
 * var state by remember { mutableStateOf(LoaderState.Idle) }
 * LoadingButton(
 *     text = "Place order",
 *     state = state,
 *     onClick = {
 *         state = LoaderState.Loading
 *         scope.launch { state = if (api.placeOrder()) LoaderState.Success else LoaderState.Error }
 *     },
 *     onResultShown = { state = LoaderState.Idle },
 * )
 * ```
 *
 * - **No double submits**: taps are ignored unless [state] is [LoaderState.Idle].
 * - **No layout jump**: the button keeps its size while it shrinks, so the screen doesn't move.
 * - **Accessible**: it stays a button with its label, and a screen reader hears
 *   "Loading", then [successLabel] or [errorLabel]. Success and failure play a haptic.
 * - [onResultShown] runs once the result has been on screen for [resultMillis]; set the state
 *   back to Idle there, or navigate away.
 *
 * @param loader What spins inside the button. Any loader in the library works; it gets the
 *   content colour and the size to draw at.
 */
@Composable
public fun LoadingButton(
    onClick: () -> Unit,
    state: LoaderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onResultShown: () -> Unit = {},
    resultMillis: Long = 1400,
    containerColor: Color = LocalLoaderColor.current,
    contentColor: Color = Color.White,
    successColor: Color = LoaderDefaults.successColor,
    errorColor: Color = LoaderDefaults.errorColor,
    height: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
    successLabel: String = LoaderDefaults.successLabel,
    errorLabel: String = LoaderDefaults.errorLabel,
    haptics: Boolean = true,
    config: LoaderConfig = LocalLoaderConfig.current,
    loader: @Composable (color: Color, size: Dp) -> Unit = { color, size ->
        ExpressiveArc(color = color, size = size, strokeWidth = size / 9f, config = config.copy(announce = false))
    },
    content: @Composable () -> Unit,
) {
    val instant = config.reducedMotion == ReducedMotion.On || LocalFrozenFrame.current != null
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val active = state != LoaderState.Idle

    val collapse = animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = if (instant) snap() else spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "collapse",
    )
    val fill by animateColorAsState(
        targetValue = when (state) {
            LoaderState.Success -> successColor
            LoaderState.Error -> errorColor
            else -> containerColor
        },
        animationSpec = if (instant) snap() else tween(260),
        label = "fill",
    )
    val press by animateFloatAsState(if (pressed && !active) 0.96f else 1f, label = "press")

    // Keep showing the last tick or loader while the button grows back to Idle.
    val lastActive = remember { mutableStateOf(LoaderState.Loading) }
    if (active) SideEffect { lastActive.value = state }
    val innerState = if (active) state else lastActive.value
    val showInner by remember { derivedStateOf { collapse.value > 0.01f } }

    val shake = remember { Animatable(1f) }
    val resultShown by rememberUpdatedState(onResultShown)
    LaunchedEffect(state) {
        if (state == LoaderState.Success || state == LoaderState.Error) {
            if (state == LoaderState.Error && !instant) {
                shake.snapTo(0f)
                shake.animateTo(1f, tween(450, easing = LinearEasing))
            }
            kotlinx.coroutines.delay(resultMillis)
            resultShown()
        } else {
            shake.snapTo(1f)
        }
    }

    val label = when (state) {
        LoaderState.Idle -> null
        LoaderState.Loading -> config.label
        LoaderState.Success -> successLabel
        LoaderState.Error -> errorLabel
    }
    Box(
        modifier
            .heightIn(min = height)
            .graphicsLayer {
                scaleX = press
                scaleY = press
                alpha = if (enabled) 1f else 0.38f
                val wobble = 1f - shake.value
                translationX = sin(shake.value * Pi * 6f) * wobble * size.height * 0.18f
            }
            .semantics(mergeDescendants = true) {
                if (label != null) {
                    stateDescription = label
                    liveRegion = LiveRegionMode.Polite
                }
            }
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button) {
                if (!active) onClick()
            }
            .drawBehind {
                val w = lerp(this.size.width, this.size.height, collapse.value)
                drawRoundRect(
                    color = fill,
                    topLeft = Offset((this.size.width - w) / 2f, 0f),
                    size = Size(w, this.size.height),
                    cornerRadius = CornerRadius(this.size.height / 2f),
                )
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        // The label stays in the semantics tree, so the button keeps its name while it loads.
        Box(Modifier.graphicsLayer { alpha = (1f - collapse.value * 2.5f).coerceIn(0f, 1f) }) { content() }
        if (showInner) {
            val inner = height * 0.5f
            ResultLoaderContent(
                state = innerState,
                modifier = Modifier
                    .clearAndSetSemantics {}
                    .graphicsLayer { alpha = smoothstep(0.55f, 1f, collapse.value) },
                color = contentColor,
                successColor = contentColor,
                errorColor = contentColor,
                size = inner,
                strokeWidth = inner / 9f,
                showRing = false,
                successLabel = successLabel,
                errorLabel = errorLabel,
                haptics = haptics,
                config = config.copy(announce = false),
                shakeMark = false,
                loader = { loader(contentColor, inner) },
            )
        }
    }
}

/**
 * [LoadingButton] with a plain text label. The library doesn't depend on Material, so pass a
 * [textStyle] that matches your theme, such as `MaterialTheme.typography.labelLarge`.
 */
@Composable
public fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    state: LoaderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onResultShown: () -> Unit = {},
    resultMillis: Long = 1400,
    containerColor: Color = LocalLoaderColor.current,
    contentColor: Color = Color.White,
    successColor: Color = LoaderDefaults.successColor,
    errorColor: Color = LoaderDefaults.errorColor,
    height: Dp = 48.dp,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    successLabel: String = LoaderDefaults.successLabel,
    errorLabel: String = LoaderDefaults.errorLabel,
    haptics: Boolean = true,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoadingButton(
        onClick = onClick,
        state = state,
        modifier = modifier,
        enabled = enabled,
        onResultShown = onResultShown,
        resultMillis = resultMillis,
        containerColor = containerColor,
        contentColor = contentColor,
        successColor = successColor,
        errorColor = errorColor,
        height = height,
        successLabel = successLabel,
        errorLabel = errorLabel,
        haptics = haptics,
        config = config,
    ) {
        BasicText(text, style = textStyle.merge(TextStyle(color = contentColor)), maxLines = 1, softWrap = false)
    }
}
