package com.zingeer.blockvalue.utils

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

object Compression {

    fun compress(data: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(data)
        val outputStream = ByteArrayOutputStream(data.size)
        deflater.finish()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val output: ByteArray = outputStream.toByteArray()
//        println("Compress")
//        println("Original: " + data.size / 1024 + " Kb")
//        println("Compressed: " + output.size / 1024 + " Kb")
        return output
    }

    fun decompress(data: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(data)
        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val output: ByteArray = outputStream.toByteArray()
//        println("Decompress")
//        println("Original: " + data.size)
//        println("Compressed: " + output.size)
        return output
    }
}