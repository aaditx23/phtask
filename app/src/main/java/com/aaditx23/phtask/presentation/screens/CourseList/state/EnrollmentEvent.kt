package com.aaditx23.phtask.presentation.screens.CourseList.state

sealed interface EnrollmentEvent {
    data object Success : EnrollmentEvent
    data class Error(val message: String) : EnrollmentEvent
}

