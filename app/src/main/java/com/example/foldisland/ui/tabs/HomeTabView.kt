package com.example.foldisland.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foldisland.core.theme.IslandColors
import com.example.foldisland.managers.CalendarEventsManager
import com.example.foldisland.managers.MediaManager
import com.example.foldisland.managers.NotesStore
import com.example.foldisland.ui.island.EqualizerBarsView

@Composable
fun HomeTabView(modifier: Modifier = Modifier) {
    val track by MediaManager.currentTrack.collectAsState()
    val equalizerBars by MediaManager.equalizerBars.collectAsState()
    val pinnedNote by NotesStore.pinnedNote.collectAsState()
    val pinnedEvent by CalendarEventsManager.pinnedEvent.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- MEDIA PLAYER CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(IslandColors.GlassSurfaceVariant)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Album art placeholder / icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IslandColors.NeonPurple.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Album Art",
                            tint = IslandColors.NeonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = IslandColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = IslandColors.TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    EqualizerBarsView(bars = equalizerBars)
                }

                // Media Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { MediaManager.skipPrevious() }) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Previous",
                            tint = IslandColors.TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(IslandColors.TextPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { MediaManager.togglePlayPause() }) {
                            Icon(
                                imageVector = if (track.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = IslandColors.PureBlack,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    IconButton(onClick = { MediaManager.skipNext() }) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Next",
                            tint = IslandColors.TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- PINNED SUMMARY CARD ---
        if (pinnedNote != null || pinnedEvent != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(IslandColors.GlassSurfaceVariant)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (pinnedNote != null) "📌 İğneli Not" else "🗓️ İğneli Etkinlik",
                        color = IslandColors.NeonOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(110.dp)
                    )
                    Text(
                        text = pinnedNote?.text ?: "${pinnedEvent?.emoji} ${pinnedEvent?.title} (${pinnedEvent?.daysRemainingText})",
                        color = IslandColors.TextPrimary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
