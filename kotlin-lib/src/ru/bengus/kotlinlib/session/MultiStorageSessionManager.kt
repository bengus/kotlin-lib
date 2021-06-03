package ru.bengus.kotlinlib.session

import org.slf4j.LoggerFactory

public class MultiStorageSessionManager<T : MultiStorageSessionInterface>(
    private val storedSessionStorage: SessionStorageInterface<T>,
    private val unstoredSessionStorage: SessionStorageInterface<T>,
    private val defaultSessionDataFactory: () -> T
) : SessionManagerInterface<T> {

    /**
     * Current transport value for nearest send.
     * In case when it stored session it's session id
     * In case when it unstored it's encoded currentSessionData
     */
    private var currentTransportValue: String? = null
    private var currentSessionData: T? = null

    private var received = false
    private var sent = false

    companion object {
        private val log = LoggerFactory.getLogger(MultiStorageSessionManager::class.qualifiedName)
    }

    private fun storageForSessionData(sessionData: T): SessionStorageInterface<T> {
        return when(sessionData.storageType) {
            MultiStorageSessionInterface.StorageType.STORED -> storedSessionStorage
            MultiStorageSessionInterface.StorageType.UNSTORED -> unstoredSessionStorage
        }
    }

    private suspend fun setDefaultSession() {
        val defaultSessionData = defaultSessionDataFactory()
        val storage = storageForSessionData(defaultSessionData)
        currentTransportValue = storage.save(null, defaultSessionData)
        currentSessionData = defaultSessionData
    }

    private suspend fun clearSessionIfNeeded() {
        if (currentSessionData != null && currentTransportValue != null) {
            val storage = storageForSessionData(currentSessionData!!)
            storage.clear(currentTransportValue!!)
        }
    }

    override suspend fun receive(transportValue: String?) {
        log.debug("received. initial transport: $transportValue")

        // if transportValue loaded and decoded successfully with unstoredSessionStorage, save it in currentSessionData
        // and work with it as unstored session
        // else trying to use transportValue as stored session id
        // to obtain currentSessionData from storedSessionStorage
        // in case when both was failed or transportValue is null use defaultSessionDataFactory, to set default session

        currentSessionData = transportValue?.let {
             unstoredSessionStorage.load(it) ?: storedSessionStorage.load(it)
        }
        if (currentSessionData != null) {
            currentTransportValue = transportValue
        } else {
            setDefaultSession()
        }

        log.debug("received. data: $currentSessionData value: $currentTransportValue")
        received = true
    }

    override suspend fun send(sendBlock: suspend (transportValue: String?) -> Unit) {
        log.debug("sent. data: $currentSessionData value: $currentTransportValue")

        // just send currentTransportValue to sendBlock
        // delegate actual sending to concrete caller's code
        sendBlock(currentTransportValue)
        sent = true
    }

    override fun get(): T? {
        log.debug("get called. data: $currentSessionData value: $currentTransportValue")

        if (!received) {
            throw TooEarlySessionGetException()
        }
        return currentSessionData
    }

    override suspend fun set(sessionData: T) {
        log.debug("set called. old value: $currentSessionData, new value: $sessionData")

        if (sent) {
            throw TooLateSessionSetException()
        }

        val storage = storageForSessionData(sessionData)
        val newTransportValue = storage.save(currentTransportValue, sessionData)
        // if currentTransportValue changed after saving, delete previous sessionData
        if (newTransportValue != currentTransportValue) {
            clearSessionIfNeeded()
        }
        currentTransportValue = newTransportValue
        currentSessionData = sessionData
    }

    override suspend fun clear() {
        log.debug("clear called. old value: $currentSessionData")

        if (sent) {
            throw TooLateSessionSetException()
        }

        clearSessionIfNeeded()
        setDefaultSession()
    }
}