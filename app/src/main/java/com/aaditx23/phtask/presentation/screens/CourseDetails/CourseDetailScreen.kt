package com.aaditx23.phtask.presentation.screens.CourseDetails

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aaditx23.phtask.presentation.components.ConfirmationDialog
import com.aaditx23.phtask.presentation.components.LoadingIndicator
import com.aaditx23.phtask.presentation.screens.CourseDetails.components.CourseDetailsContent
import com.aaditx23.phtask.presentation.screens.CourseDetails.components.ErrorContent
import com.aaditx23.phtask.presentation.screens.CourseDetails.components.NotFoundContent
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    onNavigateBack: () -> Unit,
    viewModel: CourseDetailViewModel = koinViewModel { parametersOf(courseId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEnrollDialog by remember { mutableStateOf(false) }
    var showUnenrollDialog by remember { mutableStateOf(false) }

    // Handle enrollment events
    LaunchedEffect(Unit) {
        viewModel.enrollmentEvent.collect { event ->
            when (event) {
                is EnrollmentEvent.EnrollSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = "Successfully enrolled in course!",
                        duration = SnackbarDuration.Short
                    )
                }
                is EnrollmentEvent.UnenrollSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = "Successfully unenrolled from course",
                        duration = SnackbarDuration.Short
                    )
                }
                is EnrollmentEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is CourseDetailUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is CourseDetailUiState.Success -> {
                CourseDetailsContent(
                    course = state.course,
                    isEnrolling = state.isEnrolling,
                    onEnrollClick = {
                        if (state.course.isEnrolled) {
                            showUnenrollDialog = true
                        } else {
                            showEnrollDialog = true
                        }
                    },
                    paddingValues = paddingValues
                )
            }
            is CourseDetailUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                    paddingValues = paddingValues
                )
            }
            is CourseDetailUiState.CourseNotFound -> {
                NotFoundContent(paddingValues)
            }
        }
    }

    if (showEnrollDialog) {
        val currentCourse = (uiState as? CourseDetailUiState.Success)?.course
        currentCourse?.let { course ->
            ConfirmationDialog(
                title = "Enroll in Course",
                message = "Do you want to enroll in \"${course.title}\"?",
                onConfirm = {
                    viewModel.toggleEnrollment()
                    showEnrollDialog = false
                },
                onDismiss = {
                    showEnrollDialog = false
                },
                confirmButtonText = "Enroll"
            )
        }
    }

    if (showUnenrollDialog) {
        val currentCourse = (uiState as? CourseDetailUiState.Success)?.course
        currentCourse?.let { course ->
            ConfirmationDialog(
                title = "Unenroll from Course",
                message = "Are you sure you want to unenroll from \"${course.title}\"?",
                onConfirm = {
                    viewModel.toggleEnrollment()
                    showUnenrollDialog = false
                },
                onDismiss = {
                    showUnenrollDialog = false
                },
                confirmButtonText = "Unenroll"
            )
        }
    }
}



