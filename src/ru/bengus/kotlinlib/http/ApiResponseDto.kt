package ru.bengus.kotlinlib.http

import kotlinx.serialization.*

@Serializable
class ApiResponseDto<out T> (
    @SerialName("is_successful")
    val isSuccessful: Boolean,
    @Serializable
    val data: T?
) {
    companion object {
        /**
         * В дженерике указан стринг, потому как для Nothing не находится дефолтный сериалайзер.
         * Ссаный kotlinx serialization
         */
        fun empty(): ApiResponseDto<String> = ApiResponseDto(true, null)

        fun <T> success(data: T): ApiResponseDto<T> =
            ApiResponseDto(true, data)

        fun error(error: ApiErrorResponseDto): ApiResponseDto<ApiErrorResponseDto> =
            ApiResponseDto(false, error)
    }
}