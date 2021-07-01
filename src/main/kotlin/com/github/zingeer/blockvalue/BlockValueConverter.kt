package com.github.zingeer.blockvalue

import io.netty.buffer.ByteBuf

interface BlockValueConverter<T> {

    fun serialize(src: T): ByteBuf

    fun deserialize(byteBuf: ByteBuf): T

}