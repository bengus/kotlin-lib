package ru.bengus.kotlinlib.database.sql

interface SqlDatabaseInterface {

    suspend fun <T> tx(operation: suspend (SqlSessionInterface) -> T): T

    suspend fun <T> txRequired(operation: suspend (SqlSessionInterface) -> T): T
}