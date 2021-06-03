package ru.bengus.kotlinlib.database.sql

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext
import javax.sql.DataSource

class SqlDatabase(
    private val dataSource: DataSource,
    private val dispatcher: CoroutineDispatcher
): SqlDatabaseInterface {

    /**
     * Закешированная в ThreadLocal сессия
     */
    private val sessionThreadLocal = ThreadLocal<SqlSessionInterface?>()

    private fun acquireSession(): SqlSessionInterface {
        // acquire new connection from data source
        return SqlSession(dataSource.connection)
    }

    override suspend fun <T> tx(operation: suspend (SqlSessionInterface) -> T): T {
        return withContext(dispatcher) {
            val existingSession = sessionThreadLocal.get()
            if (existingSession == null) {
                val session = acquireSession()
                session.use {
                    // Подкладываем сессию в корутин контекст
                    withContext(sessionThreadLocal.asContextElement(value = session)) {
                        session.runTransaction(operation)
                    }
                }
            } else {
                operation.invoke(existingSession)
            }
        }
    }

    override suspend fun <T> txRequired(operation: suspend (SqlSessionInterface) -> T): T {
        val existingSession = sessionThreadLocal.get()
            ?: throw IllegalStateException("This call must be run inside a tx call block")
        return operation.invoke(existingSession)
    }
}

private suspend fun <T> SqlSessionInterface.runTransaction(
    operation: suspend (SqlSessionInterface) -> T
): T {
    return try {
        operation.invoke(this).also {
            if (!this.connection.autoCommit) {
                this.connection.commit()
            }
        }
    } catch (th: Throwable) {
        if (!this.connection.autoCommit) {
            this.connection.rollback()
        }
        throw th
    }
}