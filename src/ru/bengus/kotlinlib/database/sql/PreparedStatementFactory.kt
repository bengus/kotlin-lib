package ru.bengus.kotlinlib.database.sql

import java.math.BigDecimal
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.*

class PreparedStatementFactory {

    fun createPreparedStatement(
        connection: Connection,
        query: SqlQuery,
        returnGeneratedKeys: Boolean = false
    ): PreparedStatement {
        val stmt = if (returnGeneratedKeys) {
            connection.prepareStatement(query.cleanSql, Statement.RETURN_GENERATED_KEYS)
        } else {
            connection.prepareStatement(query.cleanSql)
        }

        return populateParams(query, stmt)
    }

    private fun populateParams(query: SqlQuery, stmt: PreparedStatement): PreparedStatement {
        if (query.replacementMap.isNotEmpty()) {
            query.replacementMap.forEach { paramName, occurrences ->
                occurrences.forEach { index ->
                    setTypedParam(stmt, index + 1, query.parametersMap[paramName].sqlParam())
                }
            }
        } else {
            query.parameters.forEachIndexed { index, value ->
                setTypedParam(stmt, index + 1, value.sqlParam())
            }
        }

        return stmt
    }

    private fun <T> setTypedParam(
        stmt: PreparedStatement,
        idx: Int,
        param: SqlParameter<T>
    ) {
        if (param.value == null) {
            stmt.setNull(idx, param.sqlType())
        } else {
            setParam(stmt, idx, param.value)
        }
    }

    private fun setParam(stmt: PreparedStatement, idx: Int, v: Any?) {
        if (v == null) {
            stmt.setObject(idx, null)
        } else {
            when (v) {
                is String -> stmt.setString(idx, v)
                is Byte -> stmt.setByte(idx, v)
                is Boolean -> stmt.setBoolean(idx, v)
                is Int -> stmt.setInt(idx, v)
                is Long -> stmt.setLong(idx, v)
                is Short -> stmt.setShort(idx, v)
                is Double -> stmt.setDouble(idx, v)
                is Float -> stmt.setFloat(idx, v)
                // Always write ZonedDateTime in UTC [LocalDateTime]
                is ZonedDateTime -> stmt.setObject(idx, v.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                is LocalDateTime -> stmt.setObject(idx, v)
                is BigDecimal -> stmt.setBigDecimal(idx, v)
                is java.sql.Array -> stmt.setArray(idx, v)
                else -> stmt.setObject(idx, v)
            }
        }
    }
}
