package com.aaditx23.phtask.data.remote.repository

import com.aaditx23.phtask.data.local.Course.CourseDao
import com.aaditx23.phtask.data.remote.service.CourseApiService

class CourseRepository(
    private val courseDao: CourseDao,
    private val apiService: CourseApiService
) {

    suspend fun getCourses(){
        val list = apiService.getAllCourses()
        println(list)
    }
}