package com.aaditx23.phtask.presentation.screens.CourseList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaditx23.phtask.domain.repository.ICourseRepository
import com.aaditx23.phtask.domain.usecase.GetCoursesUseCase
import com.aaditx23.phtask.domain.usecase.SearchCoursesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CourseListViewModel(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val searchCoursesUseCase: SearchCoursesUseCase,
    private val repository: ICourseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)

    private val coursesFlow = searchQuery
//        .debounce(300)
//        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                getCoursesUseCase()
                    .map { result ->
                        result.fold(
                            onSuccess = { courses -> courses },
                            onFailure = { emptyList() }
                        )
                    }
            } else {
                searchCoursesUseCase(query)
            }
        }
        .catch { emit(emptyList()) }

    val uiState: StateFlow<CourseListUiState> = combine(
        coursesFlow,
        _isRefreshing
    ) { courses, isRefreshing ->
        CourseListUiState.Success(courses, isRefreshing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CourseListUiState.Loading
    )

    init {
        refreshCourses()
    }

    private fun refreshCourses() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshCourses()
            _isRefreshing.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}

