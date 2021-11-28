package ru.bengus.kotlinlib.utils

import java.time.format.DateTimeFormatter

object Formatting {
    val isoDateFormatter by lazy { DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx") }
}