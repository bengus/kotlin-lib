package ru.bengus.kotlinlib.session

import org.slf4j.LoggerFactory

class SessionMock<T>: SessionInterface<T> {
    private var currentSessionPayload: T? = null
    private var currentTransportValue: String? = null

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

        sendBlock(currentTransportValue)
    }

    var getCallsCount: Int = 0
    override fun get(): T? {
        getCallsCount += 1
        log.debug("get called $getCallsCount times. current value: $currentSessionPayload")

        return currentSessionPayload
    }

    var getTransportValueCallsCount: Int = 0
    override fun getTransportValue(): String? {
        getTransportValueCallsCount += 1
        log.debug("getTransportValue called $getTransportValueCallsCount times. current value: $currentTransportValue")

        return currentTransportValue
    }

    var setCallsCount: Int = 0
    override suspend fun set(sessionPayload: T) {
        setCallsCount += 1
        log.debug("set called $setCallsCount times. old value: $currentSessionPayload, new value: $sessionPayload")

        currentSessionPayload = sessionPayload
        currentTransportValue = sessionPayload.toString()
    }

    var clearCallsCount: Int = 0
    override suspend fun clear() {
        clearCallsCount += 1
        log.debug("clear called $clearCallsCount times. old value: $currentSessionPayload")

        currentSessionPayload = null
        currentTransportValue = null
    }
}
