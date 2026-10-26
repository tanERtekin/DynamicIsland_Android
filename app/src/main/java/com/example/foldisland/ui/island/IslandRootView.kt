package com.example.foldisland.ui.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import com.example.foldisland.core.model.IslandMode
import com.example.foldisland.core.overlay.IslandOverlayService
import com.example.foldisland.managers.HUDManager

/**
 * Root of the overlay. The hosting window has a FIXED size per state, so every animation
 * here is purely in-window drawing and never causes a WindowManager relayout.
 */
@Composable
fun IslandRootView(
    service: IslandOverlayService,
    onModeChanged: (IslandMode) -> Unit
) {
    val activeHUD by HUDManager.currentHUD.collectAsState()
    val islandMode by service.islandMode.collectAsState()
    val pillTopDp by service.pillTopDp.collectAsState()

    if (islandMode is IslandMode.Expanded) {
        ExpandedOverlay(
            topPaddingDp = pillTopDp,
            onDismiss = { onModeChanged(IslandMode.Collapsed) }
        )
        return
    }

    val compactState: IslandMode =
        activeHUD?.let { IslandMode.HUD(it) } ?: IslandMode.Collapsed

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedContent(
            targetState = compactState,
            transitionSpec = {
                (fadeIn(spring(stiffness = 600f)) + scaleIn(initialScale = 0.92f))
                    .togetherWith(fadeOut(spring(stiffness = 600f)))
                    .using(SizeTransform(clip = false))
            },
            contentAlignment = Alignment.TopCenter,
            label = "compactIsland"
        ) { mode ->
            when (mode) {
                is IslandMode.HUD -> HUDView(hud = mode.type)
                else -> CollapsedIslandView(
                    onExpandRequested = { onModeChanged(IslandMode.Expanded) }
                )
            }
        }
    }
}

@Composable
private fun ExpandedOverlay(
    topPaddingDp: Float,
    onDismiss: () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    // Full-screen scrim: tapping any empty area closes the island (no X button)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(spring(stiffness = 500f)) +
                    scaleIn(
                        initialScale = 0.85f,
                        transformOrigin = TransformOrigin(0.5f, 0f),
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f)
                    ),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = topPaddingDp.dp)
                    // Swallow taps on the card itself so they don't dismiss
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                ExpandedIslandView(onCollapseRequested = onDismiss)
            }
        }
    }
}
