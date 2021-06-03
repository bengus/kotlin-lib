package ru.bengus.kotlinlib.database.sql

import java.sql.Array
import java.sql.Connection

interface SqlSessionInterface: AutoCloseable {

    val connection: Connection

    fun createArrayOf(sqlTypeName: String, items: Collection<Any>): Array

    suspend fun <T> getOne(query: SqlQuery, extractor: (SqlRow) -> T?): T?

    suspend fun <T> getList(query: SqlQuery, extractor: (SqlRow) -> T?): List<T>

    suspend fun forEach(query: SqlQuery, operator: (SqlRow) -> Unit): Unit

    suspend fun execute(query: SqlQuery): Boolean

    suspend fun update(query: SqlQuery): Int

    suspend fun updateAndReturnGeneratedKey(query: SqlQuery): Long?
}