package com.aaditx23.phtask.presentation.screens.CourseDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaditx23.phtask.domain.usecase.EnrollCourseUseCase
import com.aaditx23.phtask.domain.usecase.GetCourseByIdUseCase
import com.aaditx23.phtask.domain.usecase.UnenrollCourseUseCase
import com.aaditx23.phtask.presentation.screens.CourseDetails.state.CourseDetailUiState
import com.aaditx23.phtask.presentation.screens.CourseDetails.state.EnrollmentEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseId: String,
    private val getCourseByIdUseCase: GetCourseByIdUseCase,
    private val enrollCourseUseCase: EnrollCourseUseCase,
    private val unenrollCourseUseCase: UnenrollCourseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    private val _enrollmentEvent = Channel<EnrollmentEvent>(Channel.BUFFERED)
    val enrollmentEvent = _enrollmentEvent.receiveAsFlow()

    init {
        loadCourse()
    }

    private fun loadCourse() {
        viewModelScope.launch {
            getCourseByIdUseCase(courseId)
                .catch { error ->
                    _uiState.value = CourseDetailUiState.Error(
                        error.message ?: "Failed to load course details"
                    )
                }
                .collect { course ->
                    if (course == null) {
                        _uiState.value = CourseDetailUiState.CourseNotFound
                    } else {
                        _uiState.value = CourseDetailUiState.Success(course)
                    }
                }
        }
    }

    fun toggleEnrollment() {
        val currentState = _uiState.value
        if (currentState !is CourseDetailUiState.Success) return

        val course = currentState.course

        viewModelScope.launch {
            // Set enrolling state
            _uiState.value = currentState.copy(isEnrolling = true)

            val result = if (course.isEnrolled) {
                unenrollCourseUseCase(courseId)
            } else {
                enrollCourseUseCase(courseId)
            }

            result
                .onSuccess {
                    _enrollmentEvent.send(
                        if (course.isEnrolled) {
                            EnrollmentEvent.UnenrollSuccess
                        } else {
                            EnrollmentEvent.EnrollSuccess
                        }
                    )
                }
                .onFailure { error ->
                    _enrollmentEvent.send(
                        EnrollmentEvent.Error(
                            error.message ?: "Failed to update enrollment status"
                        )
                    )
                }

            // Reset enrolling state
            val updatedState = _uiState.value
            if (updatedState is CourseDetailUiState.Success) {
                _uiState.value = updatedState.copy(isEnrolling = false)
            }
        }
    }

    fun retry() {
        _uiState.value = CourseDetailUiState.Loading
        loadCourse()
    }
}

