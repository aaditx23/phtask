package com.aaditx23.phtask.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InstructorDto(
    val name: String,
    val expertise_level: String
)
