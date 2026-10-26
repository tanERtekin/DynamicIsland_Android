package com.example.foldisland.core.model

sealed interface IslandMode {
    data object Collapsed : IslandMode
    data object Expanded : IslandMode
    data class HUD(val type: ActiveHUD) : IslandMode
}

sealed interface ActiveHUD {
    data class Volume(val level: Int, val max: Int = 100) : ActiveHUD
    data class Brightness(val level: Int, val max: Int = 100) : ActiveHUD
    data class Charging(val percentage: Int, val isWireless: Boolean = false) : ActiveHUD
    data class PinnedNotice(val emoji: String, val message: String) : ActiveHUD
}

enum class FoldDisplayMode {
    COVER_SCREEN,       // Closed Fold 7 (outer narrow display)
    INNER_MAIN_SCREEN,  // Open Fold 7 tablet display
    FLEX_MODE           // Half-folded laptop/tent orientation
}

enum class ActiveTab(val label: String, val iconName: String) {
    HOME("Ana Sayfa", "home"),
    CALENDAR("Takvim", "calendar"),
    NOTES("Notlar", "note"),
    SHELF("Raf (Fold)", "shelf"),
    TOOLS("Araçlar", "tools")
}

data class LeftWingState(
    val iconEmoji: String = "✨",
    val title: String = "Fold Island",
    val isPinned: Boolean = false
)

data class RightWingState(
    val isPlayingMedia: Boolean = false,
    val equalizerLevels: List<Float> = listOf(0.4f, 0.8f, 0.5f, 0.9f),
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false
)

data class MediaTrack(
    val title: String = "Çalınan Parça Yok",
    val artist: String = "Bilinmeyen Sanatçı",
    val isPlaying: Boolean = false,
    val progressMs: Long = 0,
    val durationMs: Long = 0,
    val appPackage: String? = null,
    val albumArt: android.graphics.Bitmap? = null
)

data class CalendarEventItem(
    val id: Long,
    val title: String,
    val emoji: String,
    val startTimeMillis: Long,
    val daysRemainingText: String,
    val isPinnedToNotch: Boolean = false
)

data class ScratchNote(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ShelfParkedItem(
    val id: String,
    val textContent: String? = null,
    val imageUri: String? = null,
    val sourceApp: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
