package ru.bengus.kotlinlib.session

class Session<T : SessionPayloadInterface>(
    private val sessionStorage: SessionStorageInterface<T>,
    defaultSessionPayloadFactory: () -> T?
) : SessionAbstract<T>(defaultSessionPayloadFactory) {

    override suspend fun loadSessionPayload(transportValue: String): T? {
        return sessionStorage.load(transportValue)
    }

    override suspend fun saveSessionPayload(transportValue: String?, sessionPayload: T): String {
        return sessionStorage.save(transportValue, sessionPayload)
    }

    override suspend fun clearSessionPayload(transportValue: String, sessionPayload: T) {
        sessionStorage.clear(transportValue)
    }
}
