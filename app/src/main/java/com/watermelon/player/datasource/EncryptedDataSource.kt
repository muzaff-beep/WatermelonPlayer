package com.watermelon.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import java.io.File
import java.io.RandomAccessFile
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptedDataSource(
    private val secretKey: ByteArray,
    private val iv: ByteArray,
    private val encryptedFile: File
) : BaseDataSource(true) {

    private var randomAccessFile: RandomAccessFile? = null
    private var bytesRemaining: Long = 0
    private var opened = false

    override fun open(dataSpec: DataSpec): Long {
        transferInitializing(dataSpec)
        randomAccessFile = RandomAccessFile(encryptedFile, "r")
        randomAccessFile!!.seek(dataSpec.position)

        bytesRemaining = if (dataSpec.length != C.LENGTH_UNBOUNDED.toLong()) {
            dataSpec.length
        } else {
            randomAccessFile!!.length() - dataSpec.position
        }

        opened = true
        transferStarted(dataSpec)
        return bytesRemaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

        val bytesToRead = minOf(bytesRemaining, length.toLong()).toInt()
        val read = randomAccessFile?.read(buffer, offset, bytesToRead) ?: -1

        if (read > 0) {
            decryptInPlace(buffer, offset, read, randomAccessFile!!.filePointer - read)
            bytesRemaining -= read
            bytesTransferred(read)
        }
        return read
    }

    private fun decryptInPlace(buffer: ByteArray, offset: Int, length: Int, filePosition: Long) {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        val keySpec = SecretKeySpec(secretKey, "AES")
        val updatedIv = calculateIvForOffset(iv, filePosition)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(updatedIv))
        cipher.update(buffer, offset, length, buffer, offset)
    }

    private fun calculateIvForOffset(baseIv: ByteArray, offset: Long): ByteArray {
        val counter = ByteBuffer.wrap(baseIv.copyOf()).order(ByteOrder.BIG_ENDIAN)
        counter.putLong(counter.position() + 8, counter.getLong(counter.position() + 8) + (offset / 16))
        return counter.array()
    }

    override fun getUri(): Uri? = Uri.fromFile(encryptedFile)

    override fun close() {
        randomAccessFile?.close()
        randomAccessFile = null
        if (opened) {
            opened = false
            transferEnded()
        }
    }
}
