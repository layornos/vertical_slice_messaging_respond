package edu.kit.ipd.sdq.respond.bpmn2

import com.google.gson.*
import edu.kit.ipd.sdq.respond.messaging.*
import org.eclipse.paho.client.mqttv3.*

interface EventCallback {
    fun onTaskStart(task: Task)
    fun onTaskComplete()
}

interface EventReceiver {
    fun register(callback: EventCallback)
}

class MqttEventReceiver(val client: MqttClient) : EventReceiver, MqttCallback {
    val callbacks = mutableListOf<EventCallback>()

    init {
        client.connect()
        client.setCallback(this)
        client.subscribe("#")
    }

    override fun register(callback: EventCallback) {
        callbacks.add(callback)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(eventTypeFactory)
            .registerTypeAdapterFactory(taskTypeFactory).create()
        val message = gson.fromJson(mqttMessage?.payload?.decodeToString(), Message::class.java)
        for (event in message.events) {
            when (event) {
                is TaskStartEvent -> {
                    callbacks.forEach { it.onTaskStart(event.task) }
                }
                is TaskCompleteEvent -> {
                    callbacks.forEach { it.onTaskComplete() }
                }
            }
        }
    }

    override fun connectionLost(cause: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("Not yet implemented")
    }
}