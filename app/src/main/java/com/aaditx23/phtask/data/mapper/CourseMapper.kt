package com.aaditx23.phtask.data.mapper

import com.aaditx23.phtask.data.local.Course.Entity.CourseEntity
import com.aaditx23.phtask.data.local.Course.Entity.Instructor as InstructorEntity
import com.aaditx23.phtask.data.remote.dto.CourseDto
import com.aaditx23.phtask.data.remote.dto.InstructorDto
import com.aaditx23.phtask.domain.model.Course
import com.aaditx23.phtask.domain.model.Instructor

fun CourseDto.toEntity(): CourseEntity {
    return CourseEntity(
        courseId = this.course_id,
        title = this.title,
        descriptionShort = this.description_short,
        instructor = this.instructor.toEntity(),
        durationWeeks = this.duration_weeks,
        priceUsd = this.price_usd,
        isPremium = this.is_premium,
        tags = this.tags,
        rating = this.rating,
    )
}

fun InstructorDto.toEntity(): InstructorEntity {
    return InstructorEntity(
        name = this.name,
        expertise_level = this.expertise_level
    )
}

fun CourseEntity.toDomain(): Course {
    return Course(
        courseId = this.courseId,
        title = this.title,
        descriptionShort = this.descriptionShort,
        instructor = this.instructor.toDomain(),
        durationWeeks = this.durationWeeks,
        priceUsd = this.priceUsd,
        isPremium = this.isPremium,
        tags = this.tags,
        rating = this.rating,
        isEnrolled = this.isEnrolled
    )
}

fun InstructorEntity.toDomain(): Instructor {
    return Instructor(
        name = this.name,
        expertiseLevel = this.expertise_level
    )
}

