package com.aaditx23.phtask.data.local.Course.Entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity (
    @PrimaryKey
    val courseId: String,
    val title: String,
    val descriptionShort: String,
    @Embedded(prefix = "instructor_")
    val instructor: Instructor,
    val durationWeeks: Int,
    val priceUsd: Double,
    val isPremium: Boolean,
    val tags: List<String>,
    val rating: Double
)

/*

[
{
"course_id": "KOTLIN-001",
"title": "Android App Development with Compose",
"description_short": "Build modern Android apps from scratch using Kotlin and Jetpack Compose.",
"instructor": {
"name": "Prof. Anika",
"expertise_level": "Senior Developer"
},
"duration_weeks": 8,
"price_usd": 49.99,
"is_premium": true,
"tags": ["Compose", "MVVM", "Coroutines"],
"rating": 4.8
},
// ... at least two more course objects to demonstrate list scrolling and filtering
]
 */