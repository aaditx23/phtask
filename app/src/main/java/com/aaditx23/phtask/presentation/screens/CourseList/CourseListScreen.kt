package com.aaditx23.phtask.presentation.screens.CourseList

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.presentation.components.ConfirmationDialog
import com.aaditx23.phtask.presentation.screens.CourseList.components.CourseCard
import com.aaditx23.phtask.presentation.screens.CourseList.components.EmptyState
import com.aaditx23.phtask.presentation.screens.CourseList.components.ErrorMessage
import com.aaditx23.phtask.presentation.components.LoadingIndicator
import com.aaditx23.phtask.presentation.components.AppBarComponent
import com.aaditx23.phtask.presentation.screens.CourseList.components.SearchBar
import com.aaditx23.phtask.presentation.screens.CourseList.components.SyncStatusIcon
import com.aaditx23.phtask.presentation.screens.CourseList.components.NetworkStatusBanner
import com.aaditx23.phtask.presentation.screens.CourseList.state.CourseListUiState
import com.aaditx23.phtask.presentation.screens.CourseList.state.EnrollmentEvent
import com.aaditx23.phtask.presentation.screens.CourseList.state.SyncStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun CourseListScreen(
    viewModel: CourseListViewModel = koinViewModel(),
    onCourseClick: (Course) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var courseToEnroll by remember { mutableStateOf<Course?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.enrollmentEvent.collect { event ->
            when (event) {
                is EnrollmentEvent.Success -> {
                    snackbarHostState.showSnackbar(
                        message = "Successfully enrolled in course!",
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

    // Show snackbar for network status changes
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { state ->
            if (state is CourseListUiState.Success) {
                when (val syncStatus = state.syncStatus) {
                    is SyncStatus.DeviceOffline -> {
                        snackbarHostState.showSnackbar(
                            message = "Device offline. Will retry when online.",
                            duration = SnackbarDuration.Short
                        )
                    }
                    is SyncStatus.NetworkError -> {
                        snackbarHostState.showSnackbar(
                            message = "Network error: ${syncStatus.message}",
                            duration = SnackbarDuration.Short
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppBarComponent(
                title = "Courses",
                actions = {
                    when (val state = uiState) {
                        is CourseListUiState.Success -> {
                            SyncStatusIcon(
                                syncStatus = state.syncStatus,
                                onRetryClick = { viewModel.onRetrySync() }
                            )
                        }
                        else -> {}
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is CourseListUiState.Loading -> {
                    LoadingIndicator(modifier = Modifier.padding(paddingValues))
                }

                is CourseListUiState.Success -> {
                    CourseListContent(
                        courses = state.courses,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::onSearchQueryChanged,
                        onClearSearch = viewModel::clearSearch,
                        onCourseClick = onCourseClick,
                        onEnrollClick = { course ->
                            courseToEnroll = course
                        },
                        paddingValues = paddingValues
                    )
                }

                is CourseListUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }

//            if (uiState is CourseListUiState.Success) {
//                NetworkStatusBanner(
//                    syncStatus = (uiState as CourseListUiState.Success).syncStatus,
//                    isConnected = (uiState as CourseListUiState.Success).isConnected,
//                    onRetry = { viewModel.onRetrySync() },
//                    modifier = Modifier
//                        .align(androidx.compose.ui.Alignment.TopCenter)
//                        .padding(top = paddingValues.calculateTopPadding())
//                )
//            }
        }
    }

    courseToEnroll?.let { course ->
        ConfirmationDialog(
            title = "Enroll in Course",
            message = "Do you want to enroll in \"${course.title}\"?",
            confirmButtonText = "Enroll",
            onConfirm = {
                viewModel.enrollInCourse(course.courseId)
                courseToEnroll = null
            },
            onDismiss = {
                courseToEnroll = null
            }
        )
    }
}

@Composable
private fun CourseListContent(
    courses: List<Course>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCourseClick: (Course) -> Unit,
    onEnrollClick: (Course) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClear = onClearSearch
            )
        }

        if (courses.isEmpty()) {
            item {
                EmptyState(
                    message = if (searchQuery.isBlank()) {
                        "No courses available yet. Check back later!"
                    } else {
                        "Try searching with different keywords"
                    }
                )
            }
        } else {
            items(
                items = courses,
                key = { it.courseId }
            ) { course ->
                CourseCard(
                    course = course,
                    onClick = onCourseClick,
                    onEnrollClick = onEnrollClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

