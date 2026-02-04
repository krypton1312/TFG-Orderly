package com.example.orderlyphone.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth")
private val KEY_TOKEN = stringPreferencesKey("jwt")

class TokenStore(private val context: Context) {

    suspend fun save(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    suspend fun load(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_TOKEN]
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.remove(KEY_TOKEN) }
    }
}
