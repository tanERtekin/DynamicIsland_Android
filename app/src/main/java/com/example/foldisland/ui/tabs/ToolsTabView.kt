package com.example.foldisland.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foldisland.core.theme.IslandColors
import com.example.foldisland.managers.TimerCenter

@Composable
fun ToolsTabView(modifier: Modifier = Modifier) {
    val waterGlasses by TimerCenter.waterGlasses.collectAsState()
    val tallyCount by TimerCenter.tallyCount.collectAsState()
    val stopwatchSeconds by TimerCenter.stopwatchSeconds.collectAsState()
    val isStopwatchRunning by TimerCenter.isStopwatchRunning.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- STOPWATCH ROW ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IslandColors.GlassSurfaceVariant)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Kronometre",
                        color = IslandColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = TimerCenter.formatSecondsToTime(stopwatchSeconds),
                        color = IslandColors.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { TimerCenter.resetStopwatch() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IslandColors.GlassBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = IslandColors.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { TimerCenter.toggleStopwatch() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isStopwatchRunning) IslandColors.NeonOrange else IslandColors.NeonGreen)
                    ) {
                        Icon(
                            imageVector = if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = IslandColors.PureBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- WATER TRACKER & TALLY COUNTER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Water Tracker Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IslandColors.GlassSurfaceVariant)
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = "Water",
                            tint = IslandColors.NeonBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Su Takibi",
                            color = IslandColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "$waterGlasses / 8",
                        color = IslandColors.NeonBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { TimerCenter.decrementWater() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrement",
                                tint = IslandColors.TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { TimerCenter.incrementWater() },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(IslandColors.NeonBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increment",
                                tint = IslandColors.PureBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Tally Counter Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IslandColors.GlassSurfaceVariant)
                    .clickable { TimerCenter.incrementTally() }
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Sayaç (Zikir)",
                        color = IslandColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "$tallyCount",
                        color = IslandColors.NeonYellow,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { TimerCenter.resetTally() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IslandColors.GlassBorderSubtle,
                            contentColor = IslandColors.TextTertiary
                        ),
                        modifier = Modifier.height(26.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sıfırla", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
