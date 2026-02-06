package com.example.orderlyphone.ui.screen.home

import com.example.orderlyphone.domain.model.response.DashboardStartResponse

sealed class HomeState {
    data object Idle : HomeState()
    data object Loading : HomeState()
    data class Success(val dashboardStartResponse: DashboardStartResponse) : HomeState()
    data class Error(val message: String) : HomeState()
}