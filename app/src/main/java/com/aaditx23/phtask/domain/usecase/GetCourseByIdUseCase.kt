package com.aaditx23.phtask.domain.usecase

import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.domain.repository.ICourseRepository
import kotlinx.coroutines.flow.Flow

class GetCourseByIdUseCase(
    private val repository: ICourseRepository
) {
    operator fun invoke(courseId: String): Flow<Course?> {
        return repository.getCourseById(courseId)
    }
}

