package com.example.foldisland.ui.island

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foldisland.core.overlay.IslandOverlayService
import com.example.foldisland.core.theme.IslandColors
import com.example.foldisland.managers.BatteryMonitor
import com.example.foldisland.managers.CalendarEventsManager
import com.example.foldisland.managers.MediaManager
import com.example.foldisland.managers.NotesStore
import com.example.foldisland.managers.TimerCenter

private val WING_WIDTH = 84.dp
private val CAMERA_GAP = 28.dp

/**
 * Collapsed pill with a CONSTANT size: both wings have a fixed width so the camera gap is always
 * exactly centered on the punch-hole and content changes (battery %, timer ticks) never resize it.
 */
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
    val isCountdownRunning by TimerCenter.isCountdownRunning.collectAsState()
    val countdownSeconds by TimerCenter.countdownRemainingSeconds.collectAsState()
    val currentTrack by MediaManager.currentTrack.collectAsState()
    val batteryPercent by BatteryMonitor.batteryPercent.collectAsState()
    val isCharging by BatteryMonitor.isCharging.collectAsState()

    Row(
        modifier = modifier
            .width(IslandOverlayService.PILL_WIDTH_DP.dp)
            .height(IslandOverlayService.PILL_HEIGHT_DP.dp)
            .clip(CircleShape)
            .background(IslandColors.PureBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onExpandRequested()
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LEFT WING (priority: note > countdown event > timer > brand) ---
        Row(
            modifier = Modifier.width(WING_WIDTH),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            val (icon, label, color) = when {
                pinnedNote != null -> Triple("📌", pinnedNote!!.text, IslandColors.TextPrimary)
                pinnedEvent != null -> Triple(
                    pinnedEvent!!.emoji, pinnedEvent!!.daysRemainingText, IslandColors.NeonOrange
                )
                isCountdownRunning -> Triple(
                    "⏳", TimerCenter.formatSecondsToTime(countdownSeconds), IslandColors.NeonYellow
                )
                isStopwatchRunning -> Triple(
                    "⏱️", TimerCenter.formatSecondsToTime(stopwatchSeconds), IslandColors.NeonGreen
                )
                else -> Triple("🏝️", "Fold", IslandColors.TextPrimary)
            }
            Text(text = icon, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // --- CAMERA CUTOUT CLEARANCE GAP (always centered) ---
        Spacer(modifier = Modifier.width(CAMERA_GAP))

        // --- RIGHT WING (media > battery) ---
        Row(
            modifier = Modifier.width(WING_WIDTH),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (currentTrack.isPlaying) {
                currentTrack.albumArt?.let { art ->
                    Image(
                        bitmap = remember(art) { art.asImageBitmap() },
                        contentDescription = "Albüm kapağı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                EqualizerBarsView(isPlaying = true)
            } else {
                Text(
                    text = "$batteryPercent%",
                    color = if (isCharging) IslandColors.NeonGreen else IslandColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Şarj oluyor",
                        tint = IslandColors.NeonGreen,
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

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(13.dp)
    ) {
        listOf(h1, h2, h3, h4).forEach { fraction ->
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
