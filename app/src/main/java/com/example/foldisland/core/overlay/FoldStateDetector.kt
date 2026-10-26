package com.example.foldisland.core.overlay

import android.app.Activity
import android.content.Context
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.example.foldisland.core.model.FoldDisplayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FoldStateDetector(private val context: Context) {

    /**
     * Observes WindowLayoutInfo for foldable features.
     * Differentiates between Cover Screen, Unfolded Inner Screen, and Flex (Half-Opened) Mode.
     */
    fun observeFoldState(activity: Activity): Flow<FoldDisplayMode> {
        return WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(activity)
            .map { layoutInfo ->
                resolveFoldMode(layoutInfo)
            }
            .distinctUntilChanged()
    }

    private fun resolveFoldMode(layoutInfo: WindowLayoutInfo): FoldDisplayMode {
        val foldingFeature = layoutInfo.displayFeatures
            .filterIsInstance<FoldingFeature>()
            .firstOrNull()

        return when {
            foldingFeature == null -> {
                // If there's no active fold feature in the window,
                // check screen width or assume cover screen / standard view
                val screenWidthDp = context.resources.configuration.screenWidthDp
                if (screenWidthDp > 600) {
                    FoldDisplayMode.INNER_MAIN_SCREEN
                } else {
                    FoldDisplayMode.COVER_SCREEN
                }
            }
            foldingFeature.state == FoldingFeature.State.HALF_OPENED -> {
                FoldDisplayMode.FLEX_MODE
            }
            foldingFeature.state == FoldingFeature.State.FLAT -> {
                FoldDisplayMode.INNER_MAIN_SCREEN
            }
            else -> FoldDisplayMode.COVER_SCREEN
        }
    }
}
