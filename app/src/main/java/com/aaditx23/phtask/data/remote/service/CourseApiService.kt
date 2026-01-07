package com.aaditx23.phtask.data.remote.service

import com.aaditx23.phtask.data.remote.dto.CourseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class CourseApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun getAllCourses(): Result<List<CourseDto>>{
        return try{
            val response = client.get(
                "$baseUrl/data/"
            )
            Result.success(response.body())
        }
        catch (e: Exception){
            Result.failure(e)
        }
    }

}