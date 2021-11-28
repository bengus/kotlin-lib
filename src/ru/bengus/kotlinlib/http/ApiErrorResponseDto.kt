package ru.bengus.kotlinlib.http

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponseDto(
    val code: String,
    val message: String?
)
