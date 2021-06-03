package ru.bengus.kotlinlib.session

interface SessionStorageInterface<T> {

    suspend fun load(id: String): T?

    suspend fun save(id: String?, value: T): String

    suspend fun clear(id: String)
}