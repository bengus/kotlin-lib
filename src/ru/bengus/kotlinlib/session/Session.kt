package ru.bengus.kotlinlib.session

import org.slf4j.LoggerFactory

class Session<T : SessionPayloadInterface>(
    private val storedSessionStorage: SessionStorageInterface<T>,
    private val unstoredSessionStorage: SessionStorageInterface<T>,
    private val defaultSessionPayloadFactory: () -> T
) : SessionInterface<T> {

    /**
     * Current transport value for nearest send.
     * In case when it stored session it's session id
     * In case when it unstored it's encoded currentSessionPayload
     */
    private var currentTransportValue: String? = null
    private var currentSessionPayload: T? = null

    private var received = false
    private var sent = false

    companion object {
        private val log = LoggerFactory.getLogger(Session::class.qualifiedName)
    }

    private fun storageForSessionPayload(sessionPayload: T): SessionStorageInterface<T> {
        return when(sessionPayload.storageType) {
            SessionPayloadInterface.StorageType.STORED -> storedSessionStorage
            SessionPayloadInterface.StorageType.UNSTORED -> unstoredSessionStorage
        }
    }

    private suspend fun setDefaultSession() {
        val defaultSessionPayload = defaultSessionPayloadFactory()
        val storage = storageForSessionPayload(defaultSessionPayload)
        currentTransportValue = storage.save(null, defaultSessionPayload)
        currentSessionPayload = defaultSessionPayload
    }

    private suspend fun clearSessionIfNeeded() {
        // obtain sessionId from currentTransportValue
        // in case if session is unstored (client-side like JWT) it is encoded session payload
        // in case if session is stored (server-side) it is session id from database
        val sessionId = currentTransportValue ?: return
        val sessionPayload = currentSessionPayload ?: return
        storageForSessionPayload(sessionPayload).clear(sessionId)
    }

    override suspend fun receive(transportValue: String?) {
        log.debug("received. initial transport: $transportValue")

        // if transportValue loaded and decoded successfully with unstoredSessionStorage, save it in currentSessionData
        // and work with it as unstored session
        // else trying to use transportValue as stored session id
        // to obtain currentSessionData from storedSessionStorage
        // in case when both was failed or transportValue is null use defaultSessionDataFactory, to set default session

        currentSessionPayload = transportValue?.let {
             unstoredSessionStorage.load(it) ?: storedSessionStorage.load(it)
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

        val storage = storageForSessionPayload(sessionPayload)
        val newTransportValue = storage.save(currentTransportValue, sessionPayload)
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