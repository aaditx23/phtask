package com.aaditx23.phtask.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val course_id: String,
    val title: String,
    val description_short: String,
    val instructor: InstructorDto,
    val duration_weeks: Int,
    val price_usd: Double,
    val is_premium: Boolean,
    val tags: List<String>,
    val rating: Double
)

