package ru.bengus.kotlinlib.session

/**
 * Encode / Decode and sign / verify unstored session data
 */
public class UnstoredSessionStorage<T>(
    private val sessionDataSerializer: SessionDataSerializerInterface<T>
): SessionStorageInterface<T> {

    override suspend fun load(id: String): T? {
        return sessionDataSerializer.decode(id)
    }

    override suspend fun save(id: String?, value: T): String {
        return sessionDataSerializer.encode(value)
    }

    override suspend fun clear(id: String) {
        // Do nothing
    }
}