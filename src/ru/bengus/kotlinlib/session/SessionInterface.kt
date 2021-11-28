package ru.bengus.kotlinlib.session

interface SessionInterface<T> {

    /**
     * Receive session transport-level value and mark session as received
     * to prevent double receive and early usage
     */
    suspend fun receive(transportValue: String?)

    /**
     * Send current session payload to transport-level and mark session as finilized
     * to prevent modifying already sent session
     */
    suspend fun send(sendBlock: suspend (transportValue: String?) -> Unit)

    /**
     * Get current session payload object
     * @throws TooEarlySessionGetException
     */
    fun get(): T?

    /**
     * Set current session payload object
     * @throws TooLateSessionSetException
     */
    suspend fun set(sessionPayload: T)

    /**
     * Clear(nullify) current session payload object
     * @throws TooLateSessionSetException
     */
    suspend fun clear()
}