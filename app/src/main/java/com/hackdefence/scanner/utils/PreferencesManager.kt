package com.hackdefence.scanner.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scanner_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        val SCANNER_CODE = stringPreferencesKey("scanner_code")
        val STAFF_NAME = stringPreferencesKey("staff_name")
        val ASSIGNED_TO = stringPreferencesKey("assigned_to")
    }

    val scannerCode: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SCANNER_CODE]
    }

    val staffName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[STAFF_NAME]
    }

    val assignedTo: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ASSIGNED_TO]
    }

    suspend fun saveScannerInfo(code: String, name: String, assignedTo: String?) {
        context.dataStore.edit { preferences ->
            preferences[SCANNER_CODE] = code
            preferences[STAFF_NAME] = name
            assignedTo?.let { preferences[ASSIGNED_TO] = it }
        }
    }

    suspend fun clearScannerInfo() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
