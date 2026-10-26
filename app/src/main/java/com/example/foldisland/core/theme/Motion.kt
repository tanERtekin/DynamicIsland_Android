package com.example.foldisland.core.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

object IslandMotion {
    // Bouncy spring for pop-in and HUD triggers
    val BouncySpringSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Smooth spring for island expand and collapse
    val ExpansionSpringSpec = spring<IntSize>(
        dampingRatio = 0.78f,
        stiffness = 380f
    )

    val OffsetSpringSpec = spring<IntOffset>(
        dampingRatio = 0.82f,
        stiffness = 400f
    )

    val ContentFadeSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
