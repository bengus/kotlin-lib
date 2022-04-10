package ru.bengus.kotlinlib.session

import org.slf4j.LoggerFactory

abstract class SessionAbstract<T : SessionPayloadInterface>(
    protected val defaultSessionPayloadFactory: () -> T?
): SessionInterface<T> {

    /**
     * Current transport value for nearest send.
     * In case when it stored session it's session id
     * In case when it unstored it's encoded currentSessionPayload
     */
    protected var currentTransportValue: String? = null
    protected var currentSessionPayload: T? = null

    protected var received = false
    protected var sent = false

    companion object {
        private val log = LoggerFactory.getLogger(SessionAbstract::class.qualifiedName)
    }

    protected abstract suspend fun loadSessionPayload(transportValue: String): T?

    protected abstract suspend fun saveSessionPayload(transportValue: String?, sessionPayload: T): String

    protected abstract suspend fun clearSessionPayload(transportValue: String, sessionPayload: T)

    private suspend fun setDefaultSession() {
        val defaultSessionPayload = defaultSessionPayloadFactory()
        if (defaultSessionPayload != null) {
            currentTransportValue = saveSessionPayload(null, defaultSessionPayload)
            currentSessionPayload = defaultSessionPayload
        } else {
            currentTransportValue = null
            currentSessionPayload = null
        }
    }

    private suspend fun clearSessionIfNeeded() {
        // obtain sessionId from currentTransportValue
        // in case if session is unstored (client-side like JWT) it is encoded session payload
        // in case if session is stored (server-side) it is session id from database
        val sessionId = currentTransportValue ?: return
        val sessionPayload = currentSessionPayload ?: return
        clearSessionPayload(sessionId, sessionPayload)
    }

    override suspend fun receive(transportValue: String?) {
        log.debug("received. initial transport: $transportValue")

        // if transportValue loaded and decoded successfully with loadPayload(transportValue:),
        // save it in currentSessionPayload and work with it
        // otherwise or if transportValue is null use defaultSessionDataFactory, to set default session

        currentSessionPayload = transportValue?.let {
            loadSessionPayload(it)
        }
        if (currentSessionPayload != null) {
            currentTransportValue = transportValue
        } else {
            setDefaultSession()
        }

        log.debug("received. data: $currentSessionPayload value: $currentTransportValue")
        received = true
    }

    override suspend fun send(sendBlock: suspend (transportValue: String?) -> Unit) {
        log.debug("sent. data: $currentSessionPayload value: $currentTransportValue")

        // just send currentTransportValue to sendBlock
        // delegate actual sending to concrete caller's code
        sendBlock(currentTransportValue)
        sent = true
    }

    override fun get(): T? {
        log.debug("get called. data: $currentSessionPayload value: $currentTransportValue")

        if (!received) {
            throw TooEarlySessionGetException()
        }
        return currentSessionPayload
    }

    override suspend fun set(sessionPayload: T) {
        log.debug("set called. old value: $currentSessionPayload, new value: $sessionPayload")

        if (sent) {
            throw TooLateSessionSetException()
        }

        val newTransportValue = saveSessionPayload(currentTransportValue, sessionPayload)
        // if currentTransportValue changed after saving, delete previous sessionData
        if (newTransportValue != currentTransportValue) {
            clearSessionIfNeeded()
        }
        currentTransportValue = newTransportValue
        currentSessionPayload = sessionPayload
    }

    override suspend fun clear() {
        log.debug("clear called. old value: $currentSessionPayload")

        if (sent) {
            throw TooLateSessionSetException()
        }

        clearSessionIfNeeded()
        setDefaultSession()
    }
}
