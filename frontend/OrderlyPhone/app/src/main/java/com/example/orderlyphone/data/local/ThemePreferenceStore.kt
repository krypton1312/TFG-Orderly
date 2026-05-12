package com.example.orderlyphone.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_preference_store")

@Singleton
class ThemePreferenceStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_preference")
    }

    val themePreference: Flow<String> = context.themeDataStore.data
        .map { prefs -> prefs[KEY_THEME] ?: "system" }

    suspend fun saveTheme(preference: String) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME] = preference
        }
    }
}
