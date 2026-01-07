package com.aaditx23.phtask.data.repository

import com.aaditx23.phtask.data.local.Course.CourseDao
import com.aaditx23.phtask.data.mapper.toDomain
import com.aaditx23.phtask.data.mapper.toEntity
import com.aaditx23.phtask.data.remote.service.CourseApiService
import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.domain.repository.ICourseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CourseRepositoryImpl(
    private val apiService: CourseApiService,
    private val courseDao: CourseDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ICourseRepository {

    override fun getCourses(): Flow<Result<List<Course>>> =
        courseDao.getAllCourses()
            .map { entities ->
                Result.success(entities.map { it.toDomain() })
            }
            .flowOn(ioDispatcher)

    override fun searchCourses(query: String): Flow<List<Course>> {
        return courseDao.searchCourses(query)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getCourseById(courseId: String): Flow<Course?> {
        return courseDao.getCourseById(courseId)
            .map { entity -> entity?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override suspend fun enrollInCourse(courseId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            courseDao.updateEnrollmentStatus(courseId, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCourses(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val networkResult = apiService.getAllCourses()

            networkResult.fold(
                onSuccess = { dtoList ->
                    val entities = dtoList.map { it.toEntity() }
                    courseDao.insertCourses(entities)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

