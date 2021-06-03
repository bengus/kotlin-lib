package ru.bengus.kotlinlib.session

public interface SessionManagerInterface<T> {

    /**
     * Receive session transport-level value and mark session as received
     * to prevent double receive and early usage
     */
    suspend fun receive(transportValue: String?)

    /**
     * Send current session data to transport-level and mark session as finilized
     * to prevent modifying already sent session
     */
    suspend fun send(sendBlock: suspend (transportValue: String?) -> Unit)

    /**
     * Get current session object
     * @throws TooEarlySessionGetException
     */
    fun get(): T?

    /**
     * Set current sessionData object
     * @throws TooLateSessionSetException
     */
    suspend fun set(sessionData: T)

    /**
     * Clear(nullify) current session object
     * @throws TooLateSessionSetException
     */
    suspend fun clear()
}