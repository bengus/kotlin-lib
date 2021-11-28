package ru.bengus.kotlinlib.session

/**
 * Encode / Decode and sign / verify unstored session payload
 */
class UnstoredSessionStorage<T>(
    encryptionKey: ByteArray,
    signKey: ByteArray,
    private val sessionPayloadSerializer: SessionPayloadSerializerInterface<T>
): SessionStorageInterface<T> {

    private val sessionDataEncoder: UnstoredSessionDataEncoder<T> by lazy {
        UnstoredSessionDataEncoder<T>(
            encryptionKey,
            signKey,
            sessionPayloadSerializer
        )
    }

    override suspend fun load(id: String): T? {
        return sessionDataEncoder.decode(id)
    }

    override suspend fun save(id: String?, value: T): String {
        return sessionDataEncoder.encode(value)
    }

    override suspend fun clear(id: String) {
        // Do nothing
    }
}