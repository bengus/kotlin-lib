package ru.bengus.kotlinlib.session

/**
 * Common interface for SessionPayload data classes.
 * It can be stored or unstored depends on business logic or content.
 */
interface SessionPayloadInterface {
    enum class StorageType {
        STORED,
        UNSTORED;
    }

    val storageType: StorageType
}