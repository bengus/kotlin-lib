package ru.bengus.kotlinlib.database.sql

data class SqlDatabaseConfig(
    val driverClassName: String,
    val dataSourceClassName: String,
    val jdbcDatabaseType: String,
    val host: String = "localhost",
    val database: String = "public",
    val port: Int,
    val user: String,
    val password: String,
) {
    val jdbcUrl: String
        get() = "jdbc:${this.jdbcDatabaseType}://${this.host}:${this.port}/${this.database}"
}