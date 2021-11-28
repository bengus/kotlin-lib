package ru.bengus.kotlinlib.database.sql

abstract class ModelSqlTableMapperAbstract<T> {

    abstract fun modelFromRow(row: SqlRow): T

    abstract fun parametersMap(model: T): Map<String, Any?>

    abstract fun filledIdCopy(model: T, id: Long): T
}