package ru.bengus.kotlinlib.session

interface SessionPayloadSerializerInterface<T> {

    suspend fun serialize(payload: T): String

    suspend fun deserialize(value: String): T?
}