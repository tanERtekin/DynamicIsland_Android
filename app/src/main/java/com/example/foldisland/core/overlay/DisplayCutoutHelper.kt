package com.example.foldisland.core.overlay

import android.graphics.Rect
import android.os.Build
import android.view.DisplayCutout
import android.view.WindowInsets
import androidx.annotation.RequiresApi

data class CutoutInfo(
    val hasCutout: Boolean,
    val boundingRect: Rect = Rect(0, 0, 0, 0),
    val centerX: Int = 0,
    val top: Int = 0,
    val height: Int = 0
)

object DisplayCutoutHelper {
    /**
     * Extracts cutout coordinates from WindowInsets if available,
     * or computes sensible defaults for Galaxy Fold 7 cover/inner displays.
     */
    fun extractCutoutInfo(insets: WindowInsets?): CutoutInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && insets != null) {
            val cutout: DisplayCutout? = insets.displayCutout
            if (cutout != null && cutout.boundingRects.isNotEmpty()) {
                val primaryRect = cutout.boundingRects[0]
                return CutoutInfo(
                    hasCutout = true,
                    boundingRect = primaryRect,
                    centerX = primaryRect.centerX(),
                    top = primaryRect.top,
                    height = primaryRect.height()
                )
            }
        }

        // Default standard centered punch-hole for modern devices like Fold 7
        return CutoutInfo(
            hasCutout = false,
            boundingRect = Rect(0, 0, 0, 0),
            centerX = 0,
            top = 24,
            height = 70
        )
    }
}
