package ru.bengus.kotlinlib.database.sql

/**
 * Прототип запроса для формирования PreparedStatement в будущем
 */
class SqlQuery private constructor(
    private val sql: String,
    val parameters: List<Any?>,
    val parametersMap: Map<String, Any?>
) {
    private val regex = Regex("""(?<!:):(?!:)[a-zA-Z]\w+""")

    /**
     * Словарь индексов параметров подстановки. WHERE t.id = ? AND t.amount > ?
     * Ключ - Имя параметра, которое указывалось в [sql] WHERE t.id = :id
     * Значение - Массив индексов, по которым он указан в [cleanSql]
     */
    val replacementMap: Map<String, List<Int>> by lazy {
        extractNamedParametersIndexes()
    }

    /**
     * Очищенный от named parameters sql
     * WHERE t.id = :id AND t.amount > :min_amount
     * превращается в
     * WHERE t.id = ? AND t.amount > ?
     */
    val cleanSql: String by lazy {
        replaceNamedParameters()
    }

    private fun replaceNamedParameters(): String {
        return regex.replace(sql, "?")
    }

    private fun extractNamedParametersIndexes(): Map<String, List<Int>> {
        return regex.findAll(sql).mapIndexed { index, group ->
            Pair(group, index)
        }.groupBy({ it.first.value.substring(1) }, { it.second })
    }

    constructor(sql: String) : this(
        sql = sql,
        parameters = listOf(),
        parametersMap = mapOf()
    )

    constructor(sql: String, parameters: List<Any?>) : this(
        sql = sql,
        parameters = parameters,
        parametersMap = mapOf()
    )

    constructor(sql: String, vararg params: Any?) : this(
        sql = sql,
        parameters = params.toList(),
        parametersMap = mapOf()
    )

    constructor(sql: String, parametersMap: Map<String, Any?>) : this(
        sql = sql,
        parameters = listOf(),
        parametersMap = parametersMap
    )
}