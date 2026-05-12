package com.example.orderlyphone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.orderlyphone.data.remote.websocket.OrderWebSocketClient
import com.example.orderlyphone.ui.navigation.AppNav
import com.example.orderlyphone.ui.screen.settings.SettingsViewModel
import com.example.orderlyphone.ui.theme.OrderlyPhoneTheme
import com.example.orderlyphone.ui.theme.ThemePreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var webSocketClient: OrderWebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val themeStr by settingsVm.theme.collectAsStateWithLifecycle()
            val themePreference = when (themeStr) {
                "dark" -> ThemePreference.DARK
                "light" -> ThemePreference.LIGHT
                else -> ThemePreference.SYSTEM
            }
            OrderlyPhoneTheme(themePreference = themePreference) {
                AppNav(webSocketClient = webSocketClient)
            }
        }
    }
}
