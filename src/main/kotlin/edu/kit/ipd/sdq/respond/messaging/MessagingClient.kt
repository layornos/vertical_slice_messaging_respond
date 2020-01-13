package edu.kit.ipd.sdq.respond.messaging

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage

abstract class MessagingClient {
    //TODO: Find a technology independent way to handle topics
    abstract fun publish(topic: String, message: ByteArray)
    abstract fun disconnect()
}

class MqttMessagingClient(private val client: MqttClient) : MessagingClient() {
    private val mutex = Mutex()

    init {
        client.connect()
    }

    @ExperimentalStdlibApi
    override fun publish(topic: String, message: ByteArray) {
       GlobalScope.launch {
           println("entered scope for ${message.decodeToString()}")
           mutex.withLock {
               client.publish(topic, MqttMessage(message))
           }
           println("left scope for ${message.decodeToString()}")
       }
    }

    override fun disconnect() {
        client.disconnect()
        client.close()
    }
}