package com.example.foldisland.ui.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.unit.dp
import com.example.foldisland.core.model.IslandMode
import com.example.foldisland.core.overlay.IslandOverlayService
import com.example.foldisland.managers.HUDManager

@Composable
fun IslandRootView(
    service: IslandOverlayService,
    onModeChanged: (IslandMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeHUD by HUDManager.currentHUD.collectAsState()
    val islandMode by service.islandMode.collectAsState()

    // Determine current visual state (HUD takes precedence over Collapsed)
    val currentDisplayMode: IslandMode = when {
        islandMode is IslandMode.Expanded -> IslandMode.Expanded
        activeHUD != null -> IslandMode.HUD(activeHUD!!)
        else -> IslandMode.Collapsed
    }

    if (currentDisplayMode is IslandMode.Expanded) {
        // Full screen scrim: tapping anywhere in the empty space collapses the island!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onModeChanged(IslandMode.Collapsed)
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Consume clicks inside the island card
                    }
            ) {
                ExpandedIslandView(
                    onCollapseRequested = {
                        onModeChanged(IslandMode.Collapsed)
                    }
                )
            }
        }
    } else {
        // Collapsed & HUD states
        Box(
            modifier = modifier.padding(top = 4.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = currentDisplayMode,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "islandStateTransition"
            ) { mode ->
                when (mode) {
                    is IslandMode.Collapsed -> {
                        CollapsedIslandView(
                            onExpandRequested = {
                                onModeChanged(IslandMode.Expanded)
                            }
                        )
                    }
                    is IslandMode.HUD -> {
                        HUDView(hud = mode.type)
                    }
                    else -> Unit
                }
            }
        }
    }
}
