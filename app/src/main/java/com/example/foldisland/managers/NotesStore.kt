package com.example.foldisland.managers

import com.example.foldisland.core.model.ScratchNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object NotesStore {
    private val _notes = MutableStateFlow<List<ScratchNote>>(
        listOf(
            ScratchNote(UUID.randomUUID().toString(), "Fold 7 sunumunu hazırla", isCompleted = false, isPinned = true),
            ScratchNote(UUID.randomUUID().toString(), "E-postaları yanıtla", isCompleted = false, isPinned = false),
            ScratchNote(UUID.randomUUID().toString(), "Market alışverişi yap", isCompleted = true, isPinned = false)
        )
    )
    val notes: StateFlow<List<ScratchNote>> = _notes.asStateFlow()

    private val _pinnedNote = MutableStateFlow<ScratchNote?>(_notes.value.firstOrNull { it.isPinned })
    val pinnedNote: StateFlow<ScratchNote?> = _pinnedNote.asStateFlow()

    fun addNote(text: String, pinImmediately: Boolean = false) {
        if (text.isBlank()) return
        val newNote = ScratchNote(
            id = UUID.randomUUID().toString(),
            text = text.trim(),
            isPinned = pinImmediately
        )
        val updated = listOf(newNote) + _notes.value
        _notes.value = updated
        if (pinImmediately) {
            pinNote(newNote)
        }
    }

    fun toggleComplete(noteId: String) {
        _notes.value = _notes.value.map {
            if (it.id == noteId) {
                val updated = it.copy(isCompleted = !it.isCompleted)
                if (updated.isCompleted && updated.isPinned) {
                    unpinNote(it.id)
                }
                updated
            } else it
        }
    }

    fun pinNote(note: ScratchNote) {
        val newPinned = if (_pinnedNote.value?.id == note.id) null else note
        _pinnedNote.value = newPinned
        _notes.value = _notes.value.map {
            it.copy(isPinned = it.id == newPinned?.id)
        }
        if (newPinned != null) {
            HUDManager.showPinnedNotice("📌", "${newPinned.text.take(16)}... iğnelendi")
        }
    }

    fun unpinNote(noteId: String) {
        if (_pinnedNote.value?.id == noteId) {
            _pinnedNote.value = null
        }
        _notes.value = _notes.value.map {
            if (it.id == noteId) it.copy(isPinned = false) else it
        }
    }

    fun deleteNote(noteId: String) {
        if (_pinnedNote.value?.id == noteId) {
            _pinnedNote.value = null
        }
        _notes.value = _notes.value.filterNot { it.id == noteId }
    }
}
