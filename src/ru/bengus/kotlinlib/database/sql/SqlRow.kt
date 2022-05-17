package ru.bengus.kotlinlib.database.sql

import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class SqlRow(
    val rs: ResultSet,
    val cursor: Int = 0
) : Sequence<SqlRow> {

    private class RowIterator(val rs: ResultSet, val position: Int) : Iterator<SqlRow> {
        override fun next(): SqlRow {
            // hasNext() should be called before next()
            return SqlRow(rs, position + 1)
        }

        override fun hasNext(): Boolean {
            // because [ResultSet] isLast is optional method
            return !rs.isClosed && rs.next()
        }
    }

    override fun iterator(): Iterator<SqlRow> {
        return RowIterator(rs, cursor)
    }

    inline fun <reified T> nullOrValue(
        value: T
    ): T? {
        return if (rs.wasNull()) null else value
    }

    // String
    fun string(columnLabel: String): String {
        return string(rs.findColumn(columnLabel))
    }
    fun stringOrNull(columnLabel: String): String? {
        return stringOrNull(rs.findColumn(columnLabel))
    }
    fun string(columnIndex: Int): String {
        return stringOrNull(columnIndex)!!
    }
    fun stringOrNull(columnIndex: Int): String? {
        return nullOrValue(rs.getString(columnIndex))
    }

    // Long
    fun long(columnLabel: String): Long {
        return long(rs.findColumn(columnLabel))
    }
    fun longOrNull(columnLabel: String): Long? {
        return longOrNull(rs.findColumn(columnLabel))
    }
    fun long(columnIndex: Int): Long {
        return longOrNull(columnIndex)!!
    }
    fun longOrNull(columnIndex: Int): Long? {
        return nullOrValue(rs.getLong(columnIndex))
    }

    // List
    inline fun <reified T>list(columnLabel: String): List<T> {
        return list(rs.findColumn(columnLabel))
    }
    inline fun <reified T>listOrNull(columnLabel: String): List<T>? {
        return listOrNull(rs.findColumn(columnLabel))
    }
    inline fun <reified T>list(columnIndex: Int): List<T> {
        return listOrNull(columnIndex)!!
    }
    inline fun <reified T>listOrNull(columnIndex: Int): List<T>? {
        val list = (rs.getArray(columnIndex)?.getArray() as? Array<*>)?.filterIsInstance<T>()
        return nullOrValue(list)
    }

    // Int
    fun int(columnLabel: String): Int {
        return int(rs.findColumn(columnLabel))
    }
    fun intOrNull(columnLabel: String): Int? {
        return intOrNull(rs.findColumn(columnLabel))
    }
    fun int(columnIndex: Int): Int {
        return intOrNull(columnIndex)!!
    }
    fun intOrNull(columnIndex: Int): Int? {
        return nullOrValue(rs.getInt(columnIndex))
    }

    // Bool
    fun boolean(columnLabel: String): Boolean {
        return boolean(rs.findColumn(columnLabel))
    }
    fun boolean(columnIndex: Int): Boolean {
        return rs.getBoolean(columnIndex)
    }

    // BigDecimal
    fun bigDecimal(columnLabel: String): BigDecimal {
        return bigDecimal(rs.findColumn(columnLabel))
    }
    fun bigDecimalOrNull(columnLabel: String): BigDecimal? {
        return bigDecimalOrNull(rs.findColumn(columnLabel))
    }
    fun bigDecimal(columnIndex: Int): BigDecimal {
        return bigDecimalOrNull(columnIndex)!!
    }
    fun bigDecimalOrNull(columnIndex: Int): BigDecimal? {
        return nullOrValue(rs.getBigDecimal(columnIndex))
    }

    // LocalDateTime
    fun localDateTime(columnLabel: String): LocalDateTime {
        return localDateTime(rs.findColumn(columnLabel))
    }
    fun localDateTimeOrNull(columnLabel: String): LocalDateTime? {
        return localDateTimeOrNull(rs.findColumn(columnLabel))
    }
    fun localDateTime(columnIndex: Int): LocalDateTime {
        return localDateTimeOrNull(columnIndex)!!
    }
    fun localDateTimeOrNull(columnIndex: Int): LocalDateTime? {
        return nullOrValue(rs.getObject(columnIndex, LocalDateTime::class.java))
    }

    // ZonedDateTime
    fun zonedDateTime(columnLabel: String, zoneId: ZoneId): ZonedDateTime {
        return zonedDateTime(rs.findColumn(columnLabel), zoneId)
    }
    fun zonedDateTimeOrNull(columnLabel: String, zoneId: ZoneId): ZonedDateTime? {
        return zonedDateTimeOrNull(rs.findColumn(columnLabel), zoneId)
    }
    fun zonedDateTime(columnIndex: Int, zoneId: ZoneId): ZonedDateTime {
        return zonedDateTimeOrNull(columnIndex, zoneId)!!
    }
    fun zonedDateTimeOrNull(columnIndex: Int, zoneId: ZoneId): ZonedDateTime? {
        return localDateTimeOrNull(columnIndex)?.atZone(zoneId)
    }

    // ZonedDateTime in UTC
    fun utcZonedDateTime(columnLabel: String): ZonedDateTime {
        return utcZonedDateTime(rs.findColumn(columnLabel))
    }
    fun utcZonedDateTimeOrNull(columnLabel: String): ZonedDateTime? {
        return utcZonedDateTimeOrNull(rs.findColumn(columnLabel))
    }
    fun utcZonedDateTime(columnIndex: Int): ZonedDateTime {
        return utcZonedDateTimeOrNull(columnIndex)!!
    }
    fun utcZonedDateTimeOrNull(columnIndex: Int): ZonedDateTime? {
        return zonedDateTimeOrNull(columnIndex, ZoneOffset.UTC)
    }
}
