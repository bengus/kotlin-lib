package ru.bengus.kotlinlib.session

import org.slf4j.LoggerFactory

class SessionMock<T>: SessionInterface<T> {
    private var currentSessionPayload: T? = null

    companion object {
        private val log = LoggerFactory.getLogger(SessionMock::class.qualifiedName)
    }

    var receiveCallsCount: Int = 0
    override suspend fun receive(transportValue: String?) {
        receiveCallsCount += 1
        log.debug("received $receiveCallsCount times: $transportValue")
    }

    var sendCallsCount: Int = 0
    override suspend fun send(sendBlock: suspend (transportValue: String?) -> Unit) {
        sendCallsCount += 1
        log.debug("sent $sendCallsCount times: ${currentSessionPayload.toString()}")

        val transportValue = currentSessionPayload.toString()
        sendBlock(transportValue)
    }

    var getCallsCount: Int = 0
    override fun get(): T? {
        getCallsCount += 1
        log.debug("get called $getCallsCount times. current value: $currentSessionPayload")

        return currentSessionPayload
    }

    var setCallsCount: Int = 0
    override suspend fun set(sessionPayload: T) {
        setCallsCount += 1
        log.debug("set called $setCallsCount times. old value: $currentSessionPayload, new value: $sessionPayload")

        currentSessionPayload = sessionPayload
    }

    var clearCallsCount: Int = 0
    override suspend fun clear() {
        clearCallsCount += 1
        log.debug("clear called $clearCallsCount times. old value: $currentSessionPayload")

        currentSessionPayload = null
    }
}