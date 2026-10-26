package com.example.foldisland.ui.island

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foldisland.core.model.ActiveTab
import com.example.foldisland.core.theme.IslandColors
import com.example.foldisland.ui.tabs.CalendarTabView
import com.example.foldisland.ui.tabs.HomeTabView
import com.example.foldisland.ui.tabs.NotesTabView
import com.example.foldisland.ui.tabs.ShelfTabView
import com.example.foldisland.ui.tabs.ToolsTabView

@Composable
fun ExpandedIslandView(
    onCollapseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedTab by remember { mutableStateOf(ActiveTab.HOME) }

    Box(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .clip(RoundedCornerShape(32.dp))
            .background(IslandColors.GlassSurface)
            .border(1.dp, IslandColors.GlassBorder, RoundedCornerShape(32.dp))
            .padding(top = 10.dp, bottom = 14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(IslandColors.NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fold 7 Dynamic Island",
                        color = IslandColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onCollapseRequested()
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(IslandColors.GlassBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = IslandColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tab Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IslandColors.GlassSurfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ActiveTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabIcon = getTabIcon(tab)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) IslandColors.PureBlack else IslandColors.GlassSurfaceVariant)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                selectedTab = tab
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = tab.label,
                                tint = if (isSelected) IslandColors.NeonGreen else IslandColors.TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tab.label,
                                    color = IslandColors.TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Active Tab Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabContent"
            ) { target ->
                when (target) {
                    ActiveTab.HOME -> HomeTabView()
                    ActiveTab.CALENDAR -> CalendarTabView()
                    ActiveTab.NOTES -> NotesTabView()
                    ActiveTab.SHELF -> ShelfTabView()
                    ActiveTab.TOOLS -> ToolsTabView()
                }
            }
        }
    }
}

private fun getTabIcon(tab: ActiveTab): ImageVector {
    return when (tab) {
        ActiveTab.HOME -> Icons.Default.Home
        ActiveTab.CALENDAR -> Icons.Default.DateRange
        ActiveTab.NOTES -> Icons.Default.Note
        ActiveTab.SHELF -> Icons.Default.FolderSpecial
        ActiveTab.TOOLS -> Icons.Default.Widgets
    }
}
