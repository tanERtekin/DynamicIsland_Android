package com.example.foldisland.managers

import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import com.example.foldisland.core.model.CalendarEventItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.concurrent.TimeUnit

object CalendarEventsManager {
    private val _events = MutableStateFlow<List<CalendarEventItem>>(emptyList())
    val events: StateFlow<List<CalendarEventItem>> = _events.asStateFlow()

    private val _pinnedEvent = MutableStateFlow<CalendarEventItem?>(null)
    val pinnedEvent: StateFlow<CalendarEventItem?> = _pinnedEvent.asStateFlow()

    init {
        // Initial sample events
        loadSampleEvents()
    }

    fun pinEvent(event: CalendarEventItem) {
        val updated = if (_pinnedEvent.value?.id == event.id) null else event
        _pinnedEvent.value = updated
        _events.value = _events.value.map {
            if (it.id == event.id) it.copy(isPinnedToNotch = updated != null)
            else it.copy(isPinnedToNotch = false)
        }
        if (updated != null) {
            HUDManager.showPinnedNotice(updated.emoji, "${updated.title} iğnelendi")
        }
    }

    fun loadDeviceCalendar(context: Context) {
        val contentResolver = context.contentResolver
        val now = System.currentTimeMillis()
        val oneYearAhead = now + (365L * 24 * 60 * 60 * 1000)

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )

        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(now.toString(), oneYearAhead.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        try {
            val cursor: Cursor? = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val list = mutableListOf<CalendarEventItem>()
                val idCol = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleCol = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val startCol = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Etkinlik"
                    val startTime = it.getLong(startCol)
                    val emoji = inferEmoji(title)
                    val countdown = formatCountdown(startTime, now)

                    list.add(
                        CalendarEventItem(
                            id = id,
                            title = title,
                            emoji = emoji,
                            startTimeMillis = startTime,
                            daysRemainingText = countdown,
                            isPinnedToNotch = _pinnedEvent.value?.id == id
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _events.value = list
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            loadSampleEvents()
        }
    }

    fun inferEmoji(title: String): String {
        val lower = title.lowercase()
        return when {
            lower.contains("doğum") || lower.contains("birthday") -> "🎂"
            lower.contains("tatil") || lower.contains("holiday") || lower.contains("yaz") -> "🏖️"
            lower.contains("uçuş") || lower.contains("flight") || lower.contains("uçak") -> "✈️"
            lower.contains("sınav") || lower.contains("exam") || lower.contains("ders") -> "📝"
            lower.contains("konser") || lower.contains("festival") || lower.contains("müzik") -> "🎟️"
            lower.contains("toplantı") || lower.contains("meeting") || lower.contains("görüşme") -> "💼"
            lower.contains("maç") || lower.contains("futbol") || lower.contains("spor") -> "🏆"
            lower.contains("sinema") || lower.contains("film") || lower.contains("dizi") -> "🎬"
            lower.contains("doktor") || lower.contains("hastane") || lower.contains("randevu") -> "🩺"
            lower.contains("yıl dönümü") || lower.contains("anniversary") -> "💍"
            else -> "📅"
        }
    }

    private fun formatCountdown(targetTime: Long, currentTime: Long): String {
        val diffMillis = targetTime - currentTime
        if (diffMillis <= 0) return "Bugün!"

        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            hours < 24 -> "${hours}s ${TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60}dk"
            days == 1L -> "Yarın"
            else -> "${days} gün"
        }
    }

    private fun loadSampleEvents() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        _events.value = listOf(
            CalendarEventItem(1, "Ege Tatili", "🏖️", now + (12 * dayMs), "12 gün"),
            CalendarEventItem(2, "Doğum Günü Kutlaması", "🎂", now + (18 * dayMs), "18 gün"),
            CalendarEventItem(3, "İstanbul Uçuşu", "✈️", now + (25 * dayMs), "25 gün"),
            CalendarEventItem(4, "Çeyrek Dönem Toplantısı", "💼", now + (3 * dayMs), "3 gün")
        )
    }
}
