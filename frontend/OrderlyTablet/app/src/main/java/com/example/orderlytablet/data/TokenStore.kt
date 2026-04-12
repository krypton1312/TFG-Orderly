package com.example.orderlytablet.data

import android.content.Context

class TokenStore(context: Context) {

    private val prefs = context.getSharedPreferences("orderly_tablet_prefs", Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) = prefs.edit().putString("access_token", token).apply()
    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun saveRefreshToken(token: String) = prefs.edit().putString("refresh_token", token).apply()
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun save(access: String, refresh: String) {
        saveAccessToken(access)
        saveRefreshToken(refresh)
    }

    fun clear() = prefs.edit().clear().apply()
}
