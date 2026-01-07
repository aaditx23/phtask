package com.aaditx23.phtask.domain.usecase

import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.domain.repository.ICourseRepository
import kotlinx.coroutines.flow.Flow

class SearchCoursesUseCase(
    private val repository: ICourseRepository
) {
    operator fun invoke(query: String): Flow<List<Course>> {
        return repository.searchCourses(query)
    }
}

