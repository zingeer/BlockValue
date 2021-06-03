package com.zingeer.blockvalue

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import java.nio.charset.Charset

@Suppress("UNCHECKED_CAST")
object BlockValueFactory {

    val types = HashMap<Class<*>, BlockValueConverter<Any>>()

    fun registry(adapter: Any, type: Class<*>): BlockValueFactory {
        types[type] = adapter as BlockValueConverter<Any>
        return this
    }

    fun deserialize(byteBuf: ByteBuf): Any {
        val size = byteBuf.readInt()
        val type = Class.forName(
            String(ByteBufUtil.getBytes(byteBuf.readBytes(size)), Charset.forName("UTF-8"))
        )
        return types[type]?.deserialize(byteBuf) ?: error("Type not found: $type")
    }

    @JvmName("deserialize_typed")
    inline fun <reified T> deserialize(byteBuf: ByteBuf): T {
        val size = byteBuf.readInt()
        byteBuf.readBytes(size)
        return (types[T::class.java]?.deserialize(byteBuf) ?: error("Type not found: ${T::class.java}")) as T
    }

    fun serialize(value: Any): ByteBuf {
        val builder = types[value::class.java] ?: error("Type not found: " + value::class.java)
        val typeInBytes = value::class.java.name.encodeToByteArray()

        return Unpooled.buffer().apply {
            writeInt(typeInBytes.size)
            writeBytes(typeInBytes)
            writeBytes(builder.serialize(value))
        }
    }
}