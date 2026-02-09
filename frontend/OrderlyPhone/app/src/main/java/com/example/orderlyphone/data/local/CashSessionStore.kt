package com.example.orderlyphone.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cashSessionDataStore by preferencesDataStore(
    name = "cash_session_store"
)

@Singleton
class CashSessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val KEY_CASH_SESSION_ID = longPreferencesKey("cash_session_id")

    val cashSessionId: Flow<Long?> =
        context.cashSessionDataStore.data.map { prefs ->
            prefs[KEY_CASH_SESSION_ID]
        }

    suspend fun saveCashSessionId(id: Long) {
        context.cashSessionDataStore.edit { prefs ->
            prefs[KEY_CASH_SESSION_ID] = id
        }
    }

    suspend fun clear() {
        context.cashSessionDataStore.edit { prefs ->
            prefs.remove(KEY_CASH_SESSION_ID)
        }
    }
}
