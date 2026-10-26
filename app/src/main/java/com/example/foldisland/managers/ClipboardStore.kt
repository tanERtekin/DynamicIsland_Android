package com.example.foldisland.managers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class ClipboardItem(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

object ClipboardStore {
    private val _history = MutableStateFlow<List<ClipboardItem>>(
        listOf(
            ClipboardItem(content = "https://github.com/android/platform-samples"),
            ClipboardItem(content = "TR56 0006 2000 0001 2345 6789 01"),
            ClipboardItem(content = "Fold 7 Dynamic Island specs draft v2")
        )
    )
    val history: StateFlow<List<ClipboardItem>> = _history.asStateFlow()

    private var clipboardManager: ClipboardManager? = null

    fun initialize(context: Context) {
        clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener {
            val primaryClip = clipboardManager?.primaryClip
            if (primaryClip != null && primaryClip.itemCount > 0) {
                val text = primaryClip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank()) {
                    addClip(text)
                }
            }
        }
    }

    fun addClip(text: String) {
        val trimmed = text.trim()
        val current = _history.value.toMutableList()
        // Deduplicate
        current.removeAll { it.content == trimmed }
        current.add(0, ClipboardItem(content = trimmed))
        // Cap at 20 items
        if (current.size > 20) {
            _history.value = current.take(20)
        } else {
            _history.value = current
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Fold Island Pano", text)
        cm?.setPrimaryClip(clip)
        HUDManager.showPinnedNotice("📋", "Panoya kopyalandı")
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
