package com.aaditx23.phtask.domain.model

data class Course(
    val courseId: String,
    val title: String,
    val descriptionShort: String,
    val instructor: Instructor,
    val durationWeeks: Int,
    val priceUsd: Double,
    val isPremium: Boolean,
    val tags: List<String>,
    val rating: Double,
    val isEnrolled: Boolean = false
)
