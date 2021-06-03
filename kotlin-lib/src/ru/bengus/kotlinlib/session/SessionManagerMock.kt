package ru.bengus.kotlinlib.session

import org.slf4j.LoggerFactory

public class SessionManagerMock<T>: SessionManagerInterface<T> {
    private var currentData: T? = null

    public companion object {
        private val log = LoggerFactory.getLogger(SessionManagerMock::class.qualifiedName)
    }

    public var receiveCallsCount: Int = 0
    override suspend fun receive(transportValue: String?) {
        receiveCallsCount += 1
        log.debug("received $receiveCallsCount times: $transportValue")
    }

    public var sendCallsCount: Int = 0
    override suspend fun send(sendBlock: suspend (transportValue: String?) -> Unit) {
        sendCallsCount += 1
        log.debug("sent $sendCallsCount times: ${currentData.toString()}")

        val transportValue = currentData.toString()
        sendBlock(transportValue)
    }

    public var getCallsCount: Int = 0
    override fun get(): T? {
        getCallsCount += 1
        log.debug("get called $getCallsCount times. current value: $currentData")

        return currentData
    }

    public var setCallsCount: Int = 0
    override suspend fun set(sessionData: T) {
        setCallsCount += 1
        log.debug("set called $setCallsCount times. old value: $currentData, new value: $sessionData")

        currentData = sessionData
    }

    public var clearCallsCount: Int = 0
    override suspend fun clear() {
        clearCallsCount += 1
        log.debug("clear called $clearCallsCount times. old value: $currentData")

        currentData = null
    }
}