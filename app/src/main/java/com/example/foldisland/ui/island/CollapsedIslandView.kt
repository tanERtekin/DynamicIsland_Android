package com.example.foldisland.ui.island

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foldisland.core.theme.IslandColors
import com.example.foldisland.managers.CalendarEventsManager
import com.example.foldisland.managers.MediaManager
import com.example.foldisland.managers.NotesStore
import com.example.foldisland.managers.BatteryMonitor
import com.example.foldisland.managers.TimerCenter

@Composable
fun CollapsedIslandView(
    onExpandRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val pinnedNote by NotesStore.pinnedNote.collectAsState()
    val pinnedEvent by CalendarEventsManager.pinnedEvent.collectAsState()
    val isStopwatchRunning by TimerCenter.isStopwatchRunning.collectAsState()
    val stopwatchSeconds by TimerCenter.stopwatchSeconds.collectAsState()
    val currentTrack by MediaManager.currentTrack.collectAsState()
    val batteryPercent by BatteryMonitor.batteryPercent.collectAsState()
    val isCharging by BatteryMonitor.isCharging.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(IslandColors.PureBlack)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onExpandRequested()
            }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(36.dp)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.wrapContentWidth()
        ) {
            // --- LEFT WING ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                when {
                    pinnedNote != null -> {
                        Text(
                            text = "📌",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = pinnedNote!!.text.take(10),
                            color = IslandColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    pinnedEvent != null -> {
                        Text(
                            text = pinnedEvent!!.emoji,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = pinnedEvent!!.daysRemainingText,
                            color = IslandColors.NeonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    isStopwatchRunning -> {
                        Text(
                            text = "⏱️",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = TimerCenter.formatSecondsToTime(stopwatchSeconds),
                            color = IslandColors.NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    else -> {
                        Text(
                            text = "🏝️ Fold",
                            color = IslandColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- CAMERA CUTOUT CLEARANCE GAP ---
            Spacer(modifier = Modifier.width(20.dp))

            // --- RIGHT WING ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp)
            ) {
                if (currentTrack.isPlaying) {
                    EqualizerBarsView(isPlaying = true)
                } else {
                        Text(
                            text = "$batteryPercent%",
                            color = if (isCharging) IslandColors.NeonGreen else IslandColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Battery",
                            tint = if (isCharging) IslandColors.NeonGreen else IslandColors.TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                }
            }
        }
    }
}

@Composable
fun EqualizerBarsView(isPlaying: Boolean) {
    if (!isPlaying) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(13.dp)
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(IslandColors.NeonGreen.copy(alpha = 0.4f))
                )
            }
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "eq")
    val h1 by transition.animateFloat(
        initialValue = 0.25f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse), label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.85f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(520, easing = LinearEasing), RepeatMode.Reverse), label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(440, easing = LinearEasing), RepeatMode.Reverse), label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 0.9f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(580, easing = LinearEasing), RepeatMode.Reverse), label = "h4"
    )

    val bars = listOf(h1, h2, h3, h4)

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(13.dp)
    ) {
        bars.forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(fraction = fraction)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(IslandColors.NeonGreen)
            )
        }
    }
}
