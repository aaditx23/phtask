package com.aaditx23.phtask.data.local.Course.Entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity (
    @PrimaryKey
    @ColumnInfo(name = "course_id")
    val courseId: String,
    val title: String,
    @ColumnInfo(name = "description_short")
    val descriptionShort: String,
    @Embedded(prefix = "instructor_")
    val instructor: Instructor,
    @ColumnInfo(name = "duration_weeks")
    val durationWeeks: Int,
    @ColumnInfo(name = "price_usd")
    val priceUsd: Double,
    @ColumnInfo(name = "is_premium")
    val isPremium: Boolean,
    val tags: List<String>,
    val rating: Double,
    @ColumnInfo(name = "is_enrolled")
    val isEnrolled: Boolean = false
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