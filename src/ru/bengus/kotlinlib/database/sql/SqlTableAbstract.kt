package ru.bengus.kotlinlib.database.sql

import org.slf4j.LoggerFactory

abstract class SqlTableAbstract<T>(
    private val database: SqlDatabaseInterface
) {

    private val logger = LoggerFactory.getLogger(SqlTableAbstract::class.qualifiedName)

    abstract val tableName: String
    abstract val primaryKeyName: String

    abstract fun modelFromRow(row: SqlRow): T

    abstract fun parametersMap(
        model: T,
        sqlSession: SqlSessionInterface
    ): Map<String, Any?>

    abstract fun filledIdCopy(model: T, id: Long): T

    suspend fun getById(id: Long): T? {
        return database.txRequired { session ->
            val query = SqlQuery(
                "SELECT * FROM $tableName WHERE $primaryKeyName = :$primaryKeyName",
                mapOf(primaryKeyName to id)
            )
            session.getOne(query, this::modelFromRow)
        }
    }

    suspend fun getAll(): List<T> {
        return database.txRequired { session ->
            val query = SqlQuery("SELECT * FROM $tableName")
            session.getList(query, this::modelFromRow)
        }
    }

    suspend fun getByIds(ids: List<Long>): List<T> {
        return database.txRequired { session ->
            val query = SqlQuery(
                "SELECT * FROM $tableName WHERE $primaryKeyName = ANY(:ids)",
                mapOf("ids" to session.createArrayOf("bigint", ids))
            )
            session.getList(query, this::modelFromRow)
        }
    }

    suspend fun save(model: T): T {
        return database.txRequired { session ->
            val parametersMap = parametersMap(
                model,
                session
            )
            val pkValue = parametersMap[primaryKeyName] as? Long ?: 0
            val filteredParametersMap = parametersMap - primaryKeyName
            if (pkValue > 0) {
                update(pkValue, filteredParametersMap)
                model
            } else {
                val insertedId = insert(filteredParametersMap)
                this.filledIdCopy(model, insertedId)
            }
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
