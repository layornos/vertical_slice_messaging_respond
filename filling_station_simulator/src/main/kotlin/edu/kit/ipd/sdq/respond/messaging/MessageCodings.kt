package edu.kit.ipd.sdq.respond.messaging

import com.google.gson.Gson
import com.google.gson.GsonBuilder

abstract class MessageCoding {
    abstract fun encode(message: Message): ByteArray
}

object JsonCoding : MessageCoding() {
    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(eventTypeFactory)
        .registerTypeAdapterFactory(taskTypeFactory).create()
    override fun encode(message: Message): ByteArray = gson.toJson(message).toByteArray()
}

