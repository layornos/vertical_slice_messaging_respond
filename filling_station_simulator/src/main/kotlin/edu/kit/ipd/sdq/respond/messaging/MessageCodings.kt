package edu.kit.ipd.sdq.respond.messaging

import com.google.gson.Gson

abstract class MessageCoding {
    abstract fun encode(message: Message): ByteArray
}

object JsonCoding : MessageCoding() {
    private val gson = Gson()
    override fun encode(message: Message): ByteArray = gson.toJson(message).toByteArray()
}

