package com.example.foldisland.ui.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                is IslandMode.Expanded -> {
                    ExpandedIslandView(
                        onCollapseRequested = {
                            onModeChanged(IslandMode.Collapsed)
                        }
                    )
                }
            }
        }
    }
}
