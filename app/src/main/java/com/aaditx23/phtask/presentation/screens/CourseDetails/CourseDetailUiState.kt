package com.aaditx23.phtask.presentation.screens.CourseDetails

import com.aaditx23.phtask.domain.model.Course

sealed interface CourseDetailUiState {
    data object Loading : CourseDetailUiState

    data class Success(
        val course: Course,
        val isEnrolling: Boolean = false
    ) : CourseDetailUiState

    data class Error(
        val message: String
    ) : CourseDetailUiState

    data object CourseNotFound : CourseDetailUiState
}

