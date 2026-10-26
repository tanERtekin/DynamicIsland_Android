package com.example.foldisland.ui.island

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foldisland.core.model.ActiveHUD
import com.example.foldisland.core.theme.IslandColors

@Composable
fun HUDView(
    hud: ActiveHUD,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(IslandColors.PureBlack)
            .height(42.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (hud) {
            is ActiveHUD.Volume -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = IslandColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    LinearProgressIndicator(
                        progress = { hud.level / hud.max.toFloat() },
                        modifier = Modifier
                            .width(110.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IslandColors.NeonBlue,
                        trackColor = IslandColors.GlassBorder
                    )
                    Text(
                        text = "${hud.level}%",
                        color = IslandColors.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            is ActiveHUD.Brightness -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Brightness",
                        tint = IslandColors.NeonYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    LinearProgressIndicator(
                        progress = { hud.level / hud.max.toFloat() },
                        modifier = Modifier
                            .width(110.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IslandColors.NeonYellow,
                        trackColor = IslandColors.GlassBorder
                    )
                    Text(
                        text = "${hud.level}%",
                        color = IslandColors.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            is ActiveHUD.Charging -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = IslandColors.NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Şarj Ediliyor %${hud.percentage}",
                        color = IslandColors.NeonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is ActiveHUD.PinnedNotice -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = hud.emoji, fontSize = 16.sp)
                    Text(
                        text = hud.message,
                        color = IslandColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
