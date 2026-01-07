package com.aaditx23.phtask.presentation.screens.CourseList

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.presentation.components.CourseCard
import com.aaditx23.phtask.presentation.components.EmptyState
import com.aaditx23.phtask.presentation.components.ErrorMessage
import com.aaditx23.phtask.presentation.components.LoadingIndicator
import com.aaditx23.phtask.presentation.components.TopAppBar
import com.aaditx23.phtask.presentation.components.SearchBar
import com.aaditx23.phtask.presentation.components.SmallLoadingIndicator
import org.koin.androidx.compose.koinViewModel

@Composable
fun CourseListScreen(
    viewModel: CourseListViewModel = koinViewModel(),
    onCourseClick: (Course) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Courses",
                actions = {
                    if (uiState is CourseListUiState.Success &&
                        (uiState as CourseListUiState.Success).isRefreshing) {
                        SmallLoadingIndicator()
                    }
                }
            )
        }
    ) { paddingValues ->
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
    }
}

@Composable
private fun CourseListContent(
    courses: List<Course>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCourseClick: (Course) -> Unit,
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

