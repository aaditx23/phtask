package com.aaditx23.phtask.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.aaditx23.phtask.domain.repository.ICourseRepository
import com.aaditx23.phtask.domain.model.Course

class GetCoursesUseCase(
private val repository: ICourseRepository
) {
    operator fun invoke(): Flow<Result<List<Course>>> {
        return repository.getCourses()
    }
}






