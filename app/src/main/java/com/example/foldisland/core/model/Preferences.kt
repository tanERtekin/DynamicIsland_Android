package com.example.foldisland.core.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.islandDataStore by preferencesDataStore(name = "island_settings")

class IslandPreferences(private val context: Context) {
    companion object {
        val KEY_NOTCH_OFFSET_X = intPreferencesKey("notch_offset_x")
        val KEY_NOTCH_OFFSET_Y = intPreferencesKey("notch_offset_y")
        val KEY_NOTCH_WIDTH_DP = intPreferencesKey("notch_width_dp")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_SHELF_ENABLED = booleanPreferencesKey("shelf_enabled")
    }

    val notchOffsetX: Flow<Int> = context.islandDataStore.data.map { it[KEY_NOTCH_OFFSET_X] ?: 0 }
    val notchOffsetY: Flow<Int> = context.islandDataStore.data.map { it[KEY_NOTCH_OFFSET_Y] ?: 0 }
    val notchWidthDp: Flow<Int> = context.islandDataStore.data.map { it[KEY_NOTCH_WIDTH_DP] ?: 180 }
    val hapticEnabled: Flow<Boolean> = context.islandDataStore.data.map { it[KEY_HAPTIC_ENABLED] ?: true }
    val shelfEnabled: Flow<Boolean> = context.islandDataStore.data.map { it[KEY_SHELF_ENABLED] ?: true }

    suspend fun updateOffsets(offsetX: Int, offsetY: Int, widthDp: Int) {
        context.islandDataStore.edit { prefs ->
            prefs[KEY_NOTCH_OFFSET_X] = offsetX
            prefs[KEY_NOTCH_OFFSET_Y] = offsetY
            prefs[KEY_NOTCH_WIDTH_DP] = widthDp
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.islandDataStore.edit { it[KEY_HAPTIC_ENABLED] = enabled }
    }
}
