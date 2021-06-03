package ru.bengus.kotlinlib.session

interface SessionDataSerializerInterface<T> {
    suspend fun encode(decoded: T): String

    suspend fun decode(encoded: String): T?
}