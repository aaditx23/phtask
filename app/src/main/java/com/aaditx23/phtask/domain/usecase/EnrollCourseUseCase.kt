package com.aaditx23.phtask.domain.usecase

import com.aaditx23.phtask.domain.repository.ICourseRepository

class EnrollCourseUseCase(
    private val repository: ICourseRepository
) {
    suspend operator fun invoke(courseId: String): Result<Unit> {
        return repository.enrollInCourse(courseId)
    }
}

