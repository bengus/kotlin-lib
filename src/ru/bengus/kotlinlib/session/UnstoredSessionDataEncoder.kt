package ru.bengus.kotlinlib.session

import org.slf4j.LoggerFactory
import java.security.*
import javax.crypto.*
import javax.crypto.spec.*
import ru.bengus.kotlinlib.utils.*

class UnstoredSessionDataEncoder<T>(
    encryptionKey: ByteArray,
    signKey: ByteArray,
    private val sessionDataSerializer: SessionPayloadSerializerInterface<T>
) {

    companion object {
        private val log = LoggerFactory.getLogger(UnstoredSessionDataEncoder::class.qualifiedName)
    }

    private val charset = Charsets.UTF_8

    private val encryptAlgorithm: String = "AES"
    private val encryptionKeySpec = SecretKeySpec(encryptionKey, encryptAlgorithm)
    /**
     * Encryption key size in bytes
     */
    val encryptionKeySize: Int get() = encryptionKeySpec.encoded.size

    private val signAlgorithm: String = "HmacSHA256"
    private val signKeySpec = SecretKeySpec(signKey, signAlgorithm)

    suspend fun encode(data: T): String {
        val transportValue = sessionDataSerializer.serialize(data)
        val ivBytes = generateInitialVector(encryptionKeySize)
        val decryptedBytes = transportValue.toByteArray(charset)
        val encryptedBytes = encrypt(ivBytes, decryptedBytes)
        val macBytes = mac(decryptedBytes)
        // session format
        // "{encrypted}.{mac}.{initialVector}"
        return "${hex(encryptedBytes)}.${hex(macBytes)}.${hex(ivBytes)}"
    }

    suspend fun decode(transportValue: String): T? {
        try {
            // session format
            // "{encrypted}.{mac}.{initialVector}"
            val components = transportValue.trim().split(".")
            if (components.size != 3) {
                return null
            }
            val encryptedBytes = hex(components[0])
            val macHex = components[1]
            val ivBytes = hex(components[2])

            val decryptedBytes = decrypt(ivBytes, encryptedBytes)

            if (hex(mac(decryptedBytes)) != macHex) {
                return null
            }
            val data = sessionDataSerializer.deserialize(decryptedBytes.toString(charset))

            return data
        } catch (e: Throwable) {
            // NumberFormatException // Invalid hex
            // InvalidAlgorithmParameterException // Invalid data
            if (log.isDebugEnabled) {
                log.debug(e.toString())
            }
            return null
        }
    }

    private fun generateInitialVector(size: Int): ByteArray {
        return ByteArray(size).apply { SecureRandom().nextBytes(this) }
    }

    private fun encrypt(initVector: ByteArray, decrypted: ByteArray): ByteArray {
        return encryptDecrypt(Cipher.ENCRYPT_MODE, initVector, decrypted)
    }

    private fun decrypt(initVector: ByteArray, encrypted: ByteArray): ByteArray {
        return encryptDecrypt(Cipher.DECRYPT_MODE, initVector, encrypted)
    }

    private fun encryptDecrypt(mode: Int, initVector: ByteArray, input: ByteArray): ByteArray {
        val iv = IvParameterSpec(initVector)
        val cipher = Cipher.getInstance("$encryptAlgorithm/CBC/PKCS5PADDING")
        cipher.init(mode, encryptionKeySpec, iv)
        return cipher.doFinal(input)
    }

    private fun mac(value: ByteArray): ByteArray = Mac.getInstance(signAlgorithm).run {
        init(signKeySpec)
        doFinal(value)
    }
}