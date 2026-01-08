package com.aaditx23.phtask.presentation.screens.CourseDetails

sealed interface EnrollmentEvent {
    data object EnrollSuccess : EnrollmentEvent
    data object UnenrollSuccess : EnrollmentEvent
    data class Error(val message: String) : EnrollmentEvent
}

