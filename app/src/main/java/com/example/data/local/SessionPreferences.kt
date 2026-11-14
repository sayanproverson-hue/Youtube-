package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "target_ssb_prefs")

class SessionPreferences(private val context: Context) {

    private object Keys {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    }

    val loggedInUserId: Flow<Long?> = context.dataStore.data.map { prefs ->
        val id = prefs[Keys.LOGGED_IN_USER_ID] ?: -1L
        if (id > 0L) id else null
    }

    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }

    suspend fun setLoggedInUserId(userId: Long?) {
        context.dataStore.edit { prefs ->
            if (userId != null && userId > 0L) {
                prefs[Keys.LOGGED_IN_USER_ID] = userId
            } else {
                prefs.remove(Keys.LOGGED_IN_USER_ID)
            }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }
}
