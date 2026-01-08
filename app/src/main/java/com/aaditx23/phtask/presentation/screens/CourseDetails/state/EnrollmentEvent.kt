package com.aaditx23.phtask.presentation.screens.CourseDetails.state

sealed interface EnrollmentEvent {
    data object EnrollSuccess : EnrollmentEvent
    data object UnenrollSuccess : EnrollmentEvent
    data class Error(val message: String) : EnrollmentEvent
}

