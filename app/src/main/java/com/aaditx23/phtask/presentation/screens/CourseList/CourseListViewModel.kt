package com.aaditx23.phtask.presentation.screens.CourseList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaditx23.phtask.data.network.NetworkMonitor
import com.aaditx23.phtask.domain.repository.ICourseRepository
import com.aaditx23.phtask.domain.usecase.EnrollCourseUseCase
import com.aaditx23.phtask.domain.usecase.GetCoursesUseCase
import com.aaditx23.phtask.domain.usecase.SearchCoursesUseCase
import com.aaditx23.phtask.presentation.screens.CourseList.state.CourseListUiState
import com.aaditx23.phtask.presentation.screens.CourseList.state.EnrollmentEvent
import com.aaditx23.phtask.presentation.screens.CourseList.state.SyncStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.invoke

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CourseListViewModel(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val searchCoursesUseCase: SearchCoursesUseCase,
    private val enrollCourseUseCase: EnrollCourseUseCase,
    private val repository: ICourseRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    private val _isConnected = MutableStateFlow(true)

    private val _enrollmentEvent = Channel<EnrollmentEvent>(Channel.BUFFERED)
    val enrollmentEvent = _enrollmentEvent.receiveAsFlow()

    private val _isEnrolling = MutableStateFlow(false)
    val isEnrolling: StateFlow<Boolean> = _isEnrolling.asStateFlow()

    private var lastFailedSync: (() -> Unit)? = null

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
        _syncStatus,
        _isConnected
    ) { courses, syncStatus, isConnected ->
        CourseListUiState.Success(courses, syncStatus, isConnected)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CourseListUiState.Loading
    )

    init {
        observeNetworkChanges()
        refreshCourses()
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkMonitor.observeNetworkState()
                .collect { isConnected ->
                    _isConnected.value = isConnected

                    when {
                        !isConnected -> {
                            if (_syncStatus.value !is SyncStatus.Success) {
                                _syncStatus.value = SyncStatus.DeviceOffline
                            }
                        }

                        lastFailedSync != null -> {
                            lastFailedSync?.invoke()
                            lastFailedSync = null
                        }

                        _syncStatus.value is SyncStatus.DeviceOffline -> {
                            refreshCourses()
                        }
                    }
                }
        }
    }



    fun refreshCourses() {
        viewModelScope.launch {
            if (!networkMonitor.isNetworkAvailable()) {
                lastFailedSync = { refreshCourses() }
                _syncStatus.value = SyncStatus.DeviceOffline
                _isConnected.value = false
                return@launch
            }

            _syncStatus.value = SyncStatus.Syncing

            repository.refreshCourses()
                .onSuccess {
                    _syncStatus.value = SyncStatus.Success
//                    kotlinx.coroutines.delay(2000)
//                    _syncStatus.value = SyncStatus.Idle
                    lastFailedSync = null
                }
                .onFailure { error ->
                    lastFailedSync = { refreshCourses() }
                    _syncStatus.value = SyncStatus.NetworkError(
                        error.message ?: "Unknown error"
                    )
                }
        }
    }

    fun onRetrySync() {
        refreshCourses()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _isEnrolling.value = true
            enrollCourseUseCase(courseId)
                .onSuccess {
                    _enrollmentEvent.send(EnrollmentEvent.Success)
                }
                .onFailure { error ->
                    _enrollmentEvent.send(
                        EnrollmentEvent.Error(
                            error.message ?: "Failed to enroll in course"
                        )
                    )
                }
            _isEnrolling.value = false
        }
    }
}

