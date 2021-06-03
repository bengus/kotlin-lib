package ru.bengus.kotlinlib.http

import kotlinx.serialization.*

@Serializable
class Response<out T> (
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
        fun empty(): Response<String> = Response(true, null)

        fun <T> success(data: T): Response<T> = Response(true, data)
    }
}