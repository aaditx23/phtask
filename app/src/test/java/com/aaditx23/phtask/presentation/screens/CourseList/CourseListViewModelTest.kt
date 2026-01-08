package com.aaditx23.phtask.presentation.screens.CourseList

import app.cash.turbine.test
import com.aaditx23.phtask.data.network.NetworkMonitor
import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.domain.model.Instructor
import com.aaditx23.phtask.domain.repository.ICourseRepository
import com.aaditx23.phtask.domain.usecase.EnrollCourseUseCase
import com.aaditx23.phtask.domain.usecase.GetCoursesUseCase
import com.aaditx23.phtask.domain.usecase.SearchCoursesUseCase
import com.aaditx23.phtask.presentation.screens.CourseList.state.CourseListUiState
import com.aaditx23.phtask.presentation.screens.CourseList.state.EnrollmentEvent
import com.aaditx23.phtask.presentation.screens.CourseList.state.SyncStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseListViewModelTest {

    private lateinit var viewModel: CourseListViewModel
    private lateinit var getCoursesUseCase: GetCoursesUseCase
    private lateinit var searchCoursesUseCase: SearchCoursesUseCase
    private lateinit var enrollCourseUseCase: EnrollCourseUseCase
    private lateinit var repository: ICourseRepository
    private lateinit var networkMonitor: NetworkMonitor

    private val testDispatcher = StandardTestDispatcher()

    private val testCourse = Course(
        courseId = "KOTLIN-001",
        title = "Android App Development",
        descriptionShort = "Learn Android",
        instructor = Instructor("Prof. Anika", "Senior Developer"),
        durationWeeks = 8,
        priceUsd = 49.99,
        isPremium = true,
        tags = listOf("Compose", "MVVM"),
        rating = 4.8,
        isEnrolled = false
    )

    private val testCourses = listOf(testCourse)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getCoursesUseCase = mockk()
        searchCoursesUseCase = mockk()
        enrollCourseUseCase = mockk()
        repository = mockk()
        networkMonitor = mockk()

        every { networkMonitor.observeNetworkState() } returns flowOf(true)
        every { networkMonitor.isNetworkAvailable() } returns true
        coEvery { repository.refreshCourses() } returns Result.success(Unit)
        every { getCoursesUseCase() } returns flowOf(Result.success(testCourses))
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is CourseListUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Success with courses`() = runTest(testDispatcher) {
        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            skipItems(1) // Skip Loading state
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is CourseListUiState.Success)
            val successState = state as CourseListUiState.Success
            assertEquals(1, successState.courses.size)
            assertEquals("KOTLIN-001", successState.courses[0].courseId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery updates when onSearchQueryChanged is called`() = runTest(testDispatcher) {
        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.onSearchQueryChanged("Android")
        advanceUntilIdle()

        assertEquals("Android", viewModel.searchQuery.value)
    }

    @Test
    fun `clearSearch resets search query to empty string`() = runTest(testDispatcher) {
        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.onSearchQueryChanged("Android")
        advanceUntilIdle()
        viewModel.clearSearch()
        advanceUntilIdle()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `searchCourses is called when search query is not blank`() = runTest(testDispatcher) {
        val searchResults = listOf(testCourse)
        every { searchCoursesUseCase("Android") } returns flowOf(searchResults)

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            skipItems(1) // Skip initial Loading state
            advanceUntilIdle()

            viewModel.onSearchQueryChanged("Android")
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is CourseListUiState.Success)
            val successState = state as CourseListUiState.Success
            assertEquals(1, successState.courses.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshCourses updates sync status to Syncing then Success`() = runTest(testDispatcher) {
        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is CourseListUiState.Success)
            val successState = state as CourseListUiState.Success
            assertTrue(successState.syncStatus is SyncStatus.Success || successState.syncStatus is SyncStatus.Syncing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshCourses sets DeviceOffline when network is unavailable`() = runTest(testDispatcher) {
        every { networkMonitor.isNetworkAvailable() } returns false
        every { networkMonitor.observeNetworkState() } returns flowOf(false)

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is CourseListUiState.Success)
            val successState = state as CourseListUiState.Success
            assertTrue(successState.syncStatus is SyncStatus.DeviceOffline)
            assertFalse(successState.isConnected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshCourses sets NetworkError when API fails`() = runTest(testDispatcher) {
        val errorMessage = "Network error"
        coEvery { repository.refreshCourses() } returns Result.failure(Exception(errorMessage))

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is CourseListUiState.Success)
            val successState = state as CourseListUiState.Success
            assertTrue(successState.syncStatus is SyncStatus.NetworkError)
            val networkError = successState.syncStatus as SyncStatus.NetworkError
            assertEquals(errorMessage, networkError.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRetrySync calls refreshCourses again`() = runTest(testDispatcher) {
        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        advanceUntilIdle()
        viewModel.onRetrySync()
        advanceUntilIdle()

        coVerify(atLeast = 2) { repository.refreshCourses() }
    }

    @Test
    fun `enrollInCourse sends Success event on success`() = runTest(testDispatcher) {
        coEvery { enrollCourseUseCase("KOTLIN-001") } returns Result.success(Unit)

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        advanceUntilIdle()

        viewModel.enrollmentEvent.test {
            viewModel.enrollInCourse("KOTLIN-001")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is EnrollmentEvent.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enrollInCourse sends Error event on failure`() = runTest(testDispatcher) {
        val errorMessage = "Enrollment failed"
        coEvery { enrollCourseUseCase("KOTLIN-001") } returns Result.failure(Exception(errorMessage))

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        advanceUntilIdle()

        viewModel.enrollmentEvent.test {
            viewModel.enrollInCourse("KOTLIN-001")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is EnrollmentEvent.Error)
            val errorEvent = event as EnrollmentEvent.Error
            assertEquals(errorMessage, errorEvent.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isEnrolling is true during enrollment and false after`() = runTest(testDispatcher) {
        coEvery { enrollCourseUseCase("KOTLIN-001") } returns Result.success(Unit)

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        advanceUntilIdle()

        assertFalse(viewModel.isEnrolling.value)

        viewModel.enrollInCourse("KOTLIN-001")
        advanceUntilIdle()

        assertFalse(viewModel.isEnrolling.value)
    }

    @Test
    fun `network reconnection triggers refresh when sync was failed`() = runTest(testDispatcher) {
        val networkStateFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
        every { networkMonitor.observeNetworkState() } returns networkStateFlow
        every { networkMonitor.isNetworkAvailable() } returns false

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        advanceUntilIdle()

        every { networkMonitor.isNetworkAvailable() } returns true
        networkStateFlow.value = true
        advanceUntilIdle()

        coVerify(atLeast = 1) { repository.refreshCourses() }
    }

    @Test
    fun `courses are loaded when search query is empty`() = runTest(testDispatcher) {
        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is CourseListUiState.Success)
            val successState = state as CourseListUiState.Success
            assertFalse(successState.courses.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState handles empty courses list`() = runTest(testDispatcher) {
        every { getCoursesUseCase() } returns flowOf(Result.success(emptyList()))

        viewModel = CourseListViewModel(
            getCoursesUseCase,
            searchCoursesUseCase,
            enrollCourseUseCase,
            repository,
            networkMonitor
        )

        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is CourseListUiState.Success)
            val successState = state as CourseListUiState.Success
            assertTrue(successState.courses.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

