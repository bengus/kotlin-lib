package ru.bengus.kotlinlib.database.sql

import org.slf4j.LoggerFactory
import java.sql.Array
import java.sql.Connection

open class SqlSession(
    override val connection: Connection
) : SqlSessionInterface {

    private val stmtFactory = PreparedStatementFactory()
    private val logger = LoggerFactory.getLogger(SqlSession::class.java)

    override fun close() {
        connection.close()
    }

    private fun <T> rows(query: SqlQuery, extractor: (SqlRow) -> T?): List<T> {
        return stmtFactory.createPreparedStatement(connection, query).use { stmt ->
            stmt.executeQuery().use { rs ->
                val rows = SqlRow(rs).map { row -> extractor.invoke(row) }
                rows.filter { r -> r != null }.map { r -> r!! }.toList()
            }
        }
    }

    override fun createArrayOf(typeName: String, items: Collection<Any>): Array {
        return connection.createArrayOf(typeName, items.toTypedArray())
    }

    override suspend fun <T> getOne(query: SqlQuery, extractor: (SqlRow) -> T?): T? {
        val rows = rows(query, extractor)
        return if (rows.isNotEmpty()) rows.first() else null
    }

    override suspend fun <T> getList(query: SqlQuery, extractor: (SqlRow) -> T?): List<T> {
        return rows(query, extractor).toList()
    }

    override suspend fun forEach(query: SqlQuery, operator: (SqlRow) -> Unit): Unit {
        stmtFactory.createPreparedStatement(connection, query).use { stmt ->
            stmt.executeQuery().use { rs ->
                SqlRow(rs).forEach { row -> operator.invoke(row) }
            }
        }
    }

    override suspend fun execute(query: SqlQuery): Boolean {
        return stmtFactory.createPreparedStatement(connection, query).use { stmt ->
            stmt.execute()
        }
    }

    override suspend fun update(query: SqlQuery): Int {
        return stmtFactory.createPreparedStatement(connection, query).use { stmt ->
            stmt.executeUpdate()
        }
    }

    override suspend fun updateAndReturnGeneratedKey(query: SqlQuery): Long? {
        return stmtFactory.createPreparedStatement(
            connection,
            query,
            true
        ).use { stmt ->
            if (stmt.executeUpdate() > 0) {
                val rs = stmt.generatedKeys
                val hasNext = rs.next()
                if (!hasNext) {
                    logger.warn("Unexpectedly, Statement#getGeneratedKeys doesn't have any elements for " + query.cleanSql)
                }
                rs.getLong(1)
            } else null
        }
    }
}
