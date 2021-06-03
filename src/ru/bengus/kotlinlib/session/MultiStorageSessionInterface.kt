package ru.bengus.kotlinlib.session

public interface MultiStorageSessionInterface {
    enum class StorageType {
        STORED,
        UNSTORED;
    }

    val storageType: StorageType
}