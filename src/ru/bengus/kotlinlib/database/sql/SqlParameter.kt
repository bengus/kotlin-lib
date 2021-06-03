package ru.bengus.kotlinlib.database.sql

class SqlParameter<out T>(
    val value: T?,
    val type: Class<out T>
)

fun <T> SqlParameter<T>.sqlType() = when (type) {
    kotlin.String::class.java,
    java.net.URL::class.java -> java.sql.Types.VARCHAR

    kotlin.Int::class.java,
    kotlin.Long::class.java,
    kotlin.Short::class.java,
    kotlin.Byte::class.java,
    java.math.BigInteger::class.java -> java.sql.Types.NUMERIC

    kotlin.Double::class.java,
    java.math.BigDecimal::class.java -> java.sql.Types.DOUBLE

    kotlin.Float::class.java -> java.sql.Types.FLOAT

    java.time.ZonedDateTime::class.java,
    java.time.Instant::class.java,
    java.time.LocalDateTime::class.java,
    java.sql.Timestamp::class.java -> java.sql.Types.TIMESTAMP

    else -> java.sql.Types.OTHER
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> T?.sqlParam(): SqlParameter<T> = when (this) {
    is SqlParameter<*> -> SqlParameter(this.value as T?, this.type as Class<T>)
    else -> SqlParameter(this, T::class.java)
}