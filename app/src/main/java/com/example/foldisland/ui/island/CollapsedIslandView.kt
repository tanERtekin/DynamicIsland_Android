package com.example.foldisland.ui.island

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foldisland.core.theme.IslandColors
import com.example.foldisland.managers.CalendarEventsManager
import com.example.foldisland.managers.MediaManager
import com.example.foldisland.managers.NotesStore
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
    val equalizerBars by MediaManager.equalizerBars.collectAsState()

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
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .height(38.dp)
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
                modifier = Modifier.padding(end = 8.dp)
            ) {
                when {
                    pinnedNote != null -> {
                        Text(
                            text = "📌",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = pinnedNote!!.text.take(12),
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
                            fontSize = 13.sp,
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
                            fontSize = 13.sp,
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
            Spacer(modifier = Modifier.width(22.dp))

            // --- RIGHT WING ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (currentTrack.isPlaying) {
                    // 4-Bar Equalizer animation
                    EqualizerBarsView(bars = equalizerBars)
                } else {
                    // Default status: Battery / Bolt
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "85%",
                            color = IslandColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Battery",
                            tint = IslandColors.NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EqualizerBarsView(bars: List<Float>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(14.dp)
    ) {
        bars.forEach { barLevel ->
            val animatedHeight by animateFloatAsState(targetValue = barLevel, label = "eq")
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(fraction = animatedHeight.coerceIn(0.15f, 1f))
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(IslandColors.NeonGreen)
            )
        }
    }
}
