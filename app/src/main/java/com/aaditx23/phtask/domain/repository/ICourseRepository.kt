package com.aaditx23.phtask.domain.repository

import com.aaditx23.phtask.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface ICourseRepository {

    fun getCourses(): Flow<Result<List<Course>>>

    fun searchCourses(query: String): Flow<List<Course>>

    fun getCourseById(courseId: String): Flow<Course?>

    suspend fun enrollInCourse(courseId: String): Result<Unit>

    suspend fun unenrollFromCourse(courseId: String): Result<Unit>

    suspend fun refreshCourses(): Result<Unit>
}

