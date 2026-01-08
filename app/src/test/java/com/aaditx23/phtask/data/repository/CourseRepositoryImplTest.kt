package com.aaditx23.phtask.data.repository

import com.aaditx23.phtask.data.local.Course.CourseDao
import com.aaditx23.phtask.data.local.Course.Entity.CourseEntity
import com.aaditx23.phtask.data.local.Course.Entity.Instructor
import com.aaditx23.phtask.data.remote.dto.CourseDto
import com.aaditx23.phtask.data.remote.dto.InstructorDto
import com.aaditx23.phtask.data.remote.service.CourseApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CourseRepositoryImplTest {

    private lateinit var repository: CourseRepositoryImpl
    private lateinit var apiService: CourseApiService
    private lateinit var courseDao: CourseDao
    private val testDispatcher = StandardTestDispatcher()

    private val testInstructorEntity = Instructor("Prof. Anika", "Senior Developer")
    private val testInstructorDto = InstructorDto("Prof. Anika", "Senior Developer")

    private val testCourseEntity = CourseEntity(
        courseId = "KOTLIN-001",
        title = "Android App Development with Compose",
        descriptionShort = "Build modern Android apps",
        instructor = testInstructorEntity,
        durationWeeks = 8,
        priceUsd = 49.99,
        isPremium = true,
        tags = listOf("Compose", "MVVM", "Coroutines"),
        rating = 4.8,
        isEnrolled = false
    )

    private val testCourseDto = CourseDto(
        course_id = "KOTLIN-001",
        title = "Android App Development with Compose",
        description_short = "Build modern Android apps",
        instructor = testInstructorDto,
        duration_weeks = 8,
        price_usd = 49.99,
        is_premium = true,
        tags = listOf("Compose", "MVVM", "Coroutines"),
        rating = 4.8
    )

    @Before
    fun setup() {
        apiService = mockk()
        courseDao = mockk(relaxed = true)
        repository = CourseRepositoryImpl(apiService, courseDao, testDispatcher)
    }

    @Test
    fun `getCourses returns success with mapped courses from database`() = runTest(testDispatcher) {
        val entities = listOf(testCourseEntity)
        every { courseDao.getAllCourses() } returns flowOf(entities)

        val result = repository.getCourses().first()

        assertTrue(result.isSuccess)
        result.onSuccess { courses ->
            assertEquals(1, courses.size)
            assertEquals("KOTLIN-001", courses[0].courseId)
            assertEquals("Android App Development with Compose", courses[0].title)
            assertEquals("Prof. Anika", courses[0].instructor.name)
            assertEquals("Senior Developer", courses[0].instructor.expertiseLevel)
            assertEquals(8, courses[0].durationWeeks)
            assertEquals(49.99, courses[0].priceUsd, 0.01)
            assertTrue(courses[0].isPremium)
            assertEquals(3, courses[0].tags.size)
            assertEquals(4.8, courses[0].rating, 0.01)
            assertFalse(courses[0].isEnrolled)
        }
    }

    @Test
    fun `getCourses returns empty list when database is empty`() = runTest(testDispatcher) {
        every { courseDao.getAllCourses() } returns flowOf(emptyList())

        val result = repository.getCourses().first()

        assertTrue(result.isSuccess)
        result.onSuccess { courses ->
            assertTrue(courses.isEmpty())
        }
    }

    @Test
    fun `searchCourses returns filtered and mapped courses`() = runTest(testDispatcher) {
        val entities = listOf(testCourseEntity)
        every { courseDao.searchCourses("Compose") } returns flowOf(entities)

        val courses = repository.searchCourses("Compose").first()

        assertEquals(1, courses.size)
        assertEquals("Android App Development with Compose", courses[0].title)
        coVerify(exactly = 1) { courseDao.searchCourses("Compose") }
    }

    @Test
    fun `searchCourses returns empty list when no matches`() = runTest(testDispatcher) {
        every { courseDao.searchCourses("Python") } returns flowOf(emptyList())

        val courses = repository.searchCourses("Python").first()

        assertTrue(courses.isEmpty())
    }

    @Test
    fun `getCourseById returns mapped course when found`() = runTest(testDispatcher) {
        every { courseDao.getCourseById("KOTLIN-001") } returns flowOf(testCourseEntity)

        val course = repository.getCourseById("KOTLIN-001").first()

        assertNotNull(course)
        assertEquals("KOTLIN-001", course?.courseId)
        assertEquals("Android App Development with Compose", course?.title)
        assertEquals("Prof. Anika", course?.instructor?.name)
    }

    @Test
    fun `getCourseById returns null when not found`() = runTest(testDispatcher) {
        every { courseDao.getCourseById("NON-EXISTENT") } returns flowOf(null)

        val course = repository.getCourseById("NON-EXISTENT").first()

        assertNull(course)
    }

    @Test
    fun `enrollInCourse returns success and updates database`() = runTest(testDispatcher) {
        coEvery { courseDao.updateEnrollmentStatus("KOTLIN-001", true) } returns Unit

        val result = repository.enrollInCourse("KOTLIN-001")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { courseDao.updateEnrollmentStatus("KOTLIN-001", true) }
    }

    @Test
    fun `enrollInCourse returns failure when database throws exception`() = runTest(testDispatcher) {
        val exception = Exception("Database error")
        coEvery { courseDao.updateEnrollmentStatus("KOTLIN-001", true) } throws exception

        val result = repository.enrollInCourse("KOTLIN-001")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `unenrollFromCourse returns success and updates database`() = runTest(testDispatcher) {
        coEvery { courseDao.updateEnrollmentStatus("KOTLIN-001", false) } returns Unit

        val result = repository.unenrollFromCourse("KOTLIN-001")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { courseDao.updateEnrollmentStatus("KOTLIN-001", false) }
    }

    @Test
    fun `unenrollFromCourse returns failure when database throws exception`() = runTest(testDispatcher) {
        val exception = Exception("Database error")
        coEvery { courseDao.updateEnrollmentStatus("KOTLIN-001", false) } throws exception

        val result = repository.unenrollFromCourse("KOTLIN-001")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `refreshCourses fetches from API and saves to database`() = runTest(testDispatcher) {
        val courseDtos = listOf(testCourseDto)
        coEvery { apiService.getAllCourses() } returns Result.success(courseDtos)
        coEvery { courseDao.upsertCourses(any()) } returns Unit

        val result = repository.refreshCourses()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { apiService.getAllCourses() }
        coVerify(exactly = 1) { courseDao.upsertCourses(any()) }
    }

    @Test
    fun `refreshCourses returns failure when API call fails`() = runTest(testDispatcher) {
        val exception = Exception("Network error")
        coEvery { apiService.getAllCourses() } returns Result.failure(exception)

        val result = repository.refreshCourses()

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { apiService.getAllCourses() }
        coVerify(exactly = 0) { courseDao.upsertCourses(any()) }
    }

    @Test
    fun `refreshCourses handles empty course list from API`() = runTest(testDispatcher) {
        coEvery { apiService.getAllCourses() } returns Result.success(emptyList())
        coEvery { courseDao.upsertCourses(emptyList()) } returns Unit

        val result = repository.refreshCourses()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { courseDao.upsertCourses(emptyList()) }
    }

    @Test
    fun `refreshCourses returns failure when database upsert throws exception`() = runTest(testDispatcher) {
        val courseDtos = listOf(testCourseDto)
        val exception = Exception("Database error")
        coEvery { apiService.getAllCourses() } returns Result.success(courseDtos)
        coEvery { courseDao.upsertCourses(any()) } throws exception

        val result = repository.refreshCourses()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getCourses preserves enrollment status in mapping`() = runTest(testDispatcher) {
        val enrolledCourse = testCourseEntity.copy(isEnrolled = true)
        every { courseDao.getAllCourses() } returns flowOf(listOf(enrolledCourse))

        val result = repository.getCourses().first()

        result.onSuccess { courses ->
            assertTrue(courses[0].isEnrolled)
        }
    }

    @Test
    fun `searchCourses maps all course properties correctly`() = runTest(testDispatcher) {
        val course = testCourseEntity.copy(
            courseId = "TEST-001",
            title = "Test Course",
            isPremium = false,
            priceUsd = 0.0,
            rating = 5.0
        )
        every { courseDao.searchCourses("Test") } returns flowOf(listOf(course))

        val courses = repository.searchCourses("Test").first()

        assertEquals("TEST-001", courses[0].courseId)
        assertEquals("Test Course", courses[0].title)
        assertFalse(courses[0].isPremium)
        assertEquals(0.0, courses[0].priceUsd, 0.01)
        assertEquals(5.0, courses[0].rating, 0.01)
    }
}

