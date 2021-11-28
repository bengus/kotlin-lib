package ru.bengus.kotlinlib.database.sql

import org.slf4j.LoggerFactory

abstract class SqlTableAbstract<T>(
    private val database: SqlDatabaseInterface,
    protected val mapper: ModelSqlTableMapperAbstract<T>
) {

    abstract val tableName: String
    abstract val primaryKeyName: String
    private val logger = LoggerFactory.getLogger(SqlTableAbstract::class.qualifiedName)

    suspend fun getById(id: Long): T? {
        return database.txRequired { session ->
            val query = SqlQuery(
                "SELECT * FROM $tableName WHERE $primaryKeyName = :$primaryKeyName",
                mapOf(primaryKeyName to id)
            )
            session.getOne(query, mapper::modelFromRow)
        }
    }

    suspend fun getAll(): List<T> {
        return database.txRequired { session ->
            val query = SqlQuery("SELECT * FROM $tableName")
            session.getList(query, mapper::modelFromRow)
        }
    }

    suspend fun getByIds(ids: List<Long>): List<T> {
        return database.txRequired { session ->
            val query = SqlQuery(
                "SELECT * FROM $tableName WHERE $primaryKeyName = ANY(:ids)",
                mapOf("ids" to session.createArrayOf("bigint", ids))
            )
            session.getList(query, mapper::modelFromRow)
        }
    }

    suspend fun save(model: T): T {
        val parametersMap = mapper.parametersMap(model)
        val pkValue = parametersMap[primaryKeyName] as? Long ?: 0
        val filteredParametersMap = parametersMap - primaryKeyName
        return if (pkValue > 0) {
            update(pkValue, filteredParametersMap)
            model
        } else {
            val insertedId = insert(filteredParametersMap)
            mapper.filledIdCopy(model, insertedId)
        }
    }

    protected suspend fun insert(parametersMap: Map<String, Any?>): Long {
        return database.txRequired { session ->
            val columns = parametersMap.keys
            val columnNames = columns.joinToString(",")
            val columnParamNames = columns.joinToString(",") { ":$it" }
            val query = SqlQuery(
                "INSERT INTO $tableName ($columnNames) VALUES ($columnParamNames)",
                parametersMap
            )
            val insertedId = session.updateAndReturnGeneratedKey(query)
            insertedId ?: throw IllegalStateException("Insert should return generated key")
        }
    }

    protected suspend fun update(id: Long, parametersMap: Map<String, Any?>): Int {
        return database.txRequired { session ->
            val columns = parametersMap.keys
            val columnSetSql = columns.joinToString(",") { "$it = :$it" }
            val query = SqlQuery(
                "UPDATE $tableName SET $columnSetSql WHERE $primaryKeyName = :$primaryKeyName",
                parametersMap.plus(primaryKeyName to id)
            )
            session.update(query)
        }
    }

    suspend fun delete(id: Long): Int {
        return database.txRequired { session ->
            val query = SqlQuery(
                "DELETE FROM $tableName WHERE $primaryKeyName = :$primaryKeyName",
                mapOf(primaryKeyName to id)
            )
            session.update(query)
        }
    }
}