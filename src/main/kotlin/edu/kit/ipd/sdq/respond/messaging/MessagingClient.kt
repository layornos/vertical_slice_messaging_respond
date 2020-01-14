package edu.kit.ipd.sdq.respond.messaging

import kotlinx.coroutines.*
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
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    init {
        client.connect()
    }

    @ExperimentalStdlibApi
    override fun publish(topic: String, message: ByteArray) {
       scope.launch {
           mutex.withLock {
               while (!client.isConnected) {
                  delay(100)
               }
               client.publish(topic, MqttMessage(message))
           }
       }
    }

    override fun disconnect() {
        client.disconnect()
        client.close()
    }
}