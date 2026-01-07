package com.aaditx23.phtask.presentation.screens.CourseList

import com.aaditx23.phtask.domain.model.Course

sealed interface CourseListUiState {
    data object Loading : CourseListUiState

    data class Success(
        val courses: List<Course>,
        val isRefreshing: Boolean = false
    ) : CourseListUiState

    data class Error(
        val message: String
    ) : CourseListUiState
}
