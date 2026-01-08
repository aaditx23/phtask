package com.aaditx23.phtask.data.remote.service

import com.aaditx23.phtask.data.remote.dto.CourseDto
import com.aaditx23.phtask.data.remote.dto.InstructorDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class CourseApiServiceTest {

    @Test
    fun `getAllCourses returns proper data`() = runTest {
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }

        val mockCourses = listOf(
            CourseDto(
                course_id = "KOTLIN-001",
                title = "Android App Development with Compose",
                description_short = "Build modern Android apps",
                instructor = InstructorDto("Prof. Anika", "Senior Developer"),
                duration_weeks = 8,
                price_usd = 49.99,
                is_premium = true,
                tags = listOf("Compose", "MVVM", "Coroutines"),
                rating = 4.8
            ),
            CourseDto(
                course_id = "KOTLIN-002",
                title = "Advanced Kotlin Programming",
                description_short = "Master Kotlin language features",
                instructor = InstructorDto("Dr. Smith", "Expert"),
                duration_weeks = 6,
                price_usd = 39.99,
                is_premium = false,
                tags = listOf("Kotlin", "Advanced"),
                rating = 4.5
            )
        )

        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(mockCourses),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val apiService = CourseApiService(client, "https://test.api")
        val result = apiService.getAllCourses()

        assertTrue(result.isSuccess)
        result.onSuccess { courses ->
            assertEquals(2, courses.size)

            assertEquals("KOTLIN-001", courses[0].course_id)
            assertEquals("Android App Development with Compose", courses[0].title)
            assertEquals("Build modern Android apps", courses[0].description_short)
            assertEquals("Prof. Anika", courses[0].instructor.name)
            assertEquals("Senior Developer", courses[0].instructor.expertise_level)
            assertEquals(8, courses[0].duration_weeks)
            assertEquals(49.99, courses[0].price_usd, 0.01)
            assertTrue(courses[0].is_premium)
            assertEquals(3, courses[0].tags.size)
            assertEquals("Compose", courses[0].tags[0])
            assertEquals(4.8, courses[0].rating, 0.01)

            assertEquals("KOTLIN-002", courses[1].course_id)
            assertEquals("Advanced Kotlin Programming", courses[1].title)
            assertEquals("Dr. Smith", courses[1].instructor.name)
            assertFalse(courses[1].is_premium)
        }

        client.close()
    }
}

