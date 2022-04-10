package ru.bengus.kotlinlib.session

class MultiStorageSession<T : SessionPayloadInterface>(
    private val storedSessionStorage: SessionStorageInterface<T>,
    private val unstoredSessionStorage: SessionStorageInterface<T>,
    private val storageForSessionPayloadBlock: (sessionPayload: T) -> SessionStorageType,
    defaultSessionPayloadFactory: () -> T?
) : SessionAbstract<T>(defaultSessionPayloadFactory) {

    private fun storageForSessionPayload(sessionPayload: T): SessionStorageInterface<T> {
        return when(storageForSessionPayloadBlock(sessionPayload)) {
            SessionStorageType.STORED -> storedSessionStorage
            SessionStorageType.UNSTORED -> unstoredSessionStorage
        }
    }

    override suspend fun loadSessionPayload(transportValue: String): T? {
        // if transportValue loaded and decoded successfully with unstoredSessionStorage
        // return payload from unstoredSessionStorage
        // else trying to use transportValue as stored session id
        // to obtain payload from storedSessionStorage

        return unstoredSessionStorage.load(transportValue)
            ?: storedSessionStorage.load(transportValue)
    }

    override suspend fun saveSessionPayload(transportValue: String?, sessionPayload: T): String {
        val storage = storageForSessionPayload(sessionPayload)
        return storage.save(transportValue, sessionPayload)
    }

    override suspend fun clearSessionPayload(transportValue: String, sessionPayload: T) {
        storageForSessionPayload(sessionPayload).clear(transportValue)
    }
}
