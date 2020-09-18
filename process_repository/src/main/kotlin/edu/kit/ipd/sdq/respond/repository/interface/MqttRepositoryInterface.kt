package edu.kit.ipd.sdq.respond.repository.`interface`

import com.google.gson.GsonBuilder
import edu.kit.ipd.sdq.respond.repository.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception

class MqttRepositoryInterface(val client: MqttClient, val repository: Repository) : MqttCallback {

    init {
        client.connect()
        client.setCallback(this)
        client.subscribe("#")
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
        try {
            val path = topic?.split("/")
            if (path?.last() == "repository") {
                val plantPath = path.subList(0, path.size-1).joinToString("/")
                val plant = repository.getPlant(plantPath)
                val gson = GsonBuilder().registerTypeAdapterFactory(commandTypeFactory).create()

                val message = gson.fromJson(mqttMessage?.payload?.decodeToString(), Command::class.java)

                val response = when (message) {
                    is AddProcessCommand -> {
                        val process = Process(message.processName, message.process, plant)
                        val processId = repository.registerProcess(process)
                        AddProcessResponse(processId, message.uuid)
                    }
                    is GetProcessCommand -> {
                        val process = repository.getProcess(message.processId, plant)
                        GetProcessResponse(process.source)
                    }
                    is RemoveProcessCommand -> {
                        repository.removeProcess(message.processId, plant)
                        null
                    }
                    is ListProcessesCommand -> {
                        val processes = repository.getProcesses(plant)
                        ListProcessesResponse(processes)
                    }
                    is RemoveAllProcessesCommand -> {
                        repository.removeAllProcesses(plant)
                        null
                    }
                    is UpdateProcessCommand -> {
                        repository.updateProcess(message.processId, plant, message.process)
                        null
                    }
                    else -> return //Unknown command. Ignoring.
                }
                response?.let {
                    client.publish(topic, gson.toJson(it, Command::class.java).asMqttMessage())
                }
            }
        }
        catch (e: Exception) {
            println(e)
        }
    }

    override fun connectionLost(cause: Throwable?) {
        client.reconnect()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
    }
}


fun String.asMqttMessage() = MqttMessage(this.toByteArray())
