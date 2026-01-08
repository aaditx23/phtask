package com.aaditx23.phtask.presentation.screens.CourseList.state

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data object Success : SyncStatus
    data class NetworkError(val message: String) : SyncStatus
    data object DeviceOffline : SyncStatus
}

