package com.aaditx23.phtask.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aaditx23.phtask.data.local.Course.CourseDao
import com.aaditx23.phtask.data.local.Course.Entity.CourseEntity
import com.aaditx23.phtask.data.local.Course.Entity.Instructor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var courseDao: CourseDao

    private val testCourse = CourseEntity(
        courseId = "TEST-001",
        title = "Test Course",
        descriptionShort = "Test Description",
        instructor = Instructor("Test Instructor", "Expert"),
        durationWeeks = 5,
        priceUsd = 49.99,
        isPremium = true,
        tags = listOf("Test", "Sample"),
        rating = 4.5,
        isEnrolled = false
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        courseDao = database.courseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertCourse_andGetAllCourses() = runBlocking {
        courseDao.insertCourse(testCourse)

        val courses = courseDao.getAllCourses().first()

        assertEquals(1, courses.size)
        assertEquals("TEST-001", courses[0].courseId)
        assertEquals("Test Course", courses[0].title)
        assertEquals("Test Instructor", courses[0].instructor.name)
    }

    @Test
    fun getAllCourses_emptyDatabase_returnsEmptyList() = runBlocking {
        val courses = courseDao.getAllCourses().first()
        assertTrue(courses.isEmpty())
    }

    @Test
    fun insertCourse_andGetById() = runBlocking {
        courseDao.insertCourse(testCourse)

        val course = courseDao.getCourseById("TEST-001").first()

        assertNotNull(course)
        assertEquals("TEST-001", course?.courseId)
        assertEquals("Test Course", course?.title)
    }

    @Test
    fun getCourseById_nonExistent_returnsNull() = runBlocking {
        val course = courseDao.getCourseById("NON-EXISTENT").first()
        assertNull(course)
    }

    @Test
    fun getCourseByIdSync_returnsExistingCourse() = runBlocking {
        courseDao.insertCourse(testCourse)

        val course = courseDao.getCourseByIdSync("TEST-001")

        assertNotNull(course)
        assertEquals("TEST-001", course?.courseId)
        assertEquals("Test Course", course?.title)
    }

    @Test
    fun getCourseByIdSync_nonExistent_returnsNull() = runBlocking {
        val course = courseDao.getCourseByIdSync("NON-EXISTENT")
        assertNull(course)
    }

    @Test
    fun searchCourses_byTitle_returnsMatchingCourses() = runBlocking {
        val course1 = testCourse.copy(courseId = "TEST-001", title = "Android Development")
        val course2 = testCourse.copy(courseId = "TEST-002", title = "Kotlin Basics")
        val course3 = testCourse.copy(courseId = "TEST-003", title = "Android Compose")

        courseDao.insertCourses(listOf(course1, course2, course3))

        val results = courseDao.searchCourses("Android").first()

        assertEquals(2, results.size)
        assertTrue(results.any { it.title == "Android Development" })
        assertTrue(results.any { it.title == "Android Compose" })
    }

    @Test
    fun searchCourses_byTag_returnsMatchingCourses() = runBlocking {
        val course1 = testCourse.copy(courseId = "TEST-001", tags = listOf("Kotlin", "Android"))
        val course2 = testCourse.copy(courseId = "TEST-002", tags = listOf("Java", "Backend"))
        val course3 = testCourse.copy(courseId = "TEST-003", tags = listOf("Kotlin", "Compose"))

        courseDao.insertCourses(listOf(course1, course2, course3))

        val results = courseDao.searchCourses("Kotlin").first()

        assertEquals(2, results.size)
        assertTrue(results.any { it.courseId == "TEST-001" })
        assertTrue(results.any { it.courseId == "TEST-003" })
    }

    @Test
    fun searchCourses_caseInsensitive() = runBlocking {
        val course = testCourse.copy(title = "Android Development")
        courseDao.insertCourse(course)

        val results = courseDao.searchCourses("android").first()

        assertEquals(1, results.size)
        assertEquals("Android Development", results[0].title)
    }

    @Test
    fun searchCourses_emptyQuery_returnsAllCourses() = runBlocking {
        courseDao.insertCourses(listOf(
            testCourse.copy(courseId = "TEST-001"),
            testCourse.copy(courseId = "TEST-002"),
            testCourse.copy(courseId = "TEST-003")
        ))

        val results = courseDao.searchCourses("").first()

        assertEquals(3, results.size)
    }

    @Test
    fun searchCourses_noMatches_returnsEmptyList() = runBlocking {
        courseDao.insertCourse(testCourse)

        val results = courseDao.searchCourses("Python").first()

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchCourses_resultsSortedByTitle() = runBlocking {
        val course1 = testCourse.copy(courseId = "TEST-001", title = "Zebra Course")
        val course2 = testCourse.copy(courseId = "TEST-002", title = "Apple Course")
        val course3 = testCourse.copy(courseId = "TEST-003", title = "Mango Course")

        courseDao.insertCourses(listOf(course1, course2, course3))

        val results = courseDao.searchCourses("Course").first()

        assertEquals(3, results.size)
        assertEquals("Apple Course", results[0].title)
        assertEquals("Mango Course", results[1].title)
        assertEquals("Zebra Course", results[2].title)
    }

    @Test
    fun updateEnrollmentStatus_enrollsCourse() = runBlocking {
        courseDao.insertCourse(testCourse)

        courseDao.updateEnrollmentStatus("TEST-001", true)

        val course = courseDao.getCourseById("TEST-001").first()
        assertTrue(course?.isEnrolled == true)
    }

    @Test
    fun updateEnrollmentStatus_unenrollsCourse() = runBlocking {
        val enrolledCourse = testCourse.copy(isEnrolled = true)
        courseDao.insertCourse(enrolledCourse)

        courseDao.updateEnrollmentStatus("TEST-001", false)

        val course = courseDao.getCourseById("TEST-001").first()
        assertFalse(course?.isEnrolled == true)
    }

    @Test
    fun updateEnrollmentStatus_doesNotAffectOtherCourses() = runBlocking {
        val course1 = testCourse.copy(courseId = "TEST-001")
        val course2 = testCourse.copy(courseId = "TEST-002")
        courseDao.insertCourses(listOf(course1, course2))

        courseDao.updateEnrollmentStatus("TEST-001", true)

        val updated = courseDao.getCourseById("TEST-001").first()
        val unchanged = courseDao.getCourseById("TEST-002").first()

        assertTrue(updated?.isEnrolled == true)
        assertFalse(unchanged?.isEnrolled == true)
    }

    @Test
    fun insertCourses_insertsMultipleCourses() = runBlocking {
        val courses = listOf(
            testCourse.copy(courseId = "TEST-001"),
            testCourse.copy(courseId = "TEST-002"),
            testCourse.copy(courseId = "TEST-003")
        )

        courseDao.insertCourses(courses)

        val allCourses = courseDao.getAllCourses().first()
        assertEquals(3, allCourses.size)
    }

    @Test
    fun insertCourse_replaceOnConflict() = runBlocking {
        courseDao.insertCourse(testCourse)

        val updatedCourse = testCourse.copy(title = "Updated Title", rating = 5.0)
        courseDao.insertCourse(updatedCourse)

        val allCourses = courseDao.getAllCourses().first()
        assertEquals(1, allCourses.size)
        assertEquals("Updated Title", allCourses[0].title)
        assertEquals(5.0, allCourses[0].rating, 0.01)
    }

    @Test
    fun upsertCourses_preservesEnrollmentForExistingCourse() = runBlocking {
        courseDao.insertCourse(testCourse)
        courseDao.updateEnrollmentStatus("TEST-001", true)

        val updatedCourse = testCourse.copy(title = "Updated Title", rating = 5.0)
        courseDao.upsertCourses(listOf(updatedCourse))

        val course = courseDao.getCourseById("TEST-001").first()
        assertTrue(course!=null)
        assertEquals("Updated Title", course?.title)
        course?.let { assertEquals(5.0, it.rating, 0.01) }
        assertTrue(course?.isEnrolled == true)
    }

    @Test
    fun upsertCourses_setsDefaultEnrollmentForNewCourse() = runBlocking {
        courseDao.upsertCourses(listOf(testCourse))

        val course = courseDao.getCourseById("TEST-001").first()
        assertFalse(course?.isEnrolled == true)
    }

    @Test
    fun upsertCourses_handlesMultipleCoursesMixed() = runBlocking {
        val course1 = testCourse.copy(courseId = "EXISTING-001")
        courseDao.insertCourse(course1)
        courseDao.updateEnrollmentStatus("EXISTING-001", true)

        val updatedCourse1 = course1.copy(title = "Updated Course 1")
        val newCourse2 = testCourse.copy(courseId = "NEW-002", title = "New Course 2")

        courseDao.upsertCourses(listOf(updatedCourse1, newCourse2))

        val allCourses = courseDao.getAllCourses().first()
        assertEquals(2, allCourses.size)

        val existingUpdated = courseDao.getCourseById("EXISTING-001").first()
        assertEquals("Updated Course 1", existingUpdated?.title)
        assertTrue(existingUpdated?.isEnrolled == true)

        val newInserted = courseDao.getCourseById("NEW-002").first()
        assertEquals("New Course 2", newInserted?.title)
        assertFalse(newInserted?.isEnrolled == true)
    }

    @Test
    fun upsertCourses_emptyList() = runBlocking {
        courseDao.insertCourse(testCourse)

        courseDao.upsertCourses(emptyList())

        val allCourses = courseDao.getAllCourses().first()
        assertEquals(1, allCourses.size)
    }

    @Test
    fun deleteAll_removesAllCourses() = runBlocking {
        val courses = listOf(
            testCourse.copy(courseId = "TEST-001"),
            testCourse.copy(courseId = "TEST-002"),
            testCourse.copy(courseId = "TEST-003")
        )
        courseDao.insertCourses(courses)

        courseDao.deleteAll()

        val allCourses = courseDao.getAllCourses().first()
        assertTrue(allCourses.isEmpty())
    }

    @Test
    fun deleteAll_emptyDatabase_doesNotThrow() = runBlocking {
        courseDao.deleteAll()

        val allCourses = courseDao.getAllCourses().first()
        assertTrue(allCourses.isEmpty())
    }

    @Test
    fun getAllCourses_sortedByTitleAscending() = runBlocking {
        val courses = listOf(
            testCourse.copy(courseId = "TEST-001", title = "Zebra Course"),
            testCourse.copy(courseId = "TEST-002", title = "Apple Course"),
            testCourse.copy(courseId = "TEST-003", title = "Mango Course")
        )
        courseDao.insertCourses(courses)

        val allCourses = courseDao.getAllCourses().first()

        assertEquals("Apple Course", allCourses[0].title)
        assertEquals("Mango Course", allCourses[1].title)
        assertEquals("Zebra Course", allCourses[2].title)
    }
}

