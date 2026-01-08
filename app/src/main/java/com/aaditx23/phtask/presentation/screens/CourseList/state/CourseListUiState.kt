package com.aaditx23.phtask.presentation.screens.CourseList.state

import com.aaditx23.phtask.domain.model.Course

sealed interface CourseListUiState {
    data object Loading : CourseListUiState

    data class Success(
        val courses: List<Course>,
        val syncStatus: SyncStatus = SyncStatus.Idle,
        val isConnected: Boolean = true
    ) : CourseListUiState

    data class Error(
        val message: String
    ) : CourseListUiState
}

