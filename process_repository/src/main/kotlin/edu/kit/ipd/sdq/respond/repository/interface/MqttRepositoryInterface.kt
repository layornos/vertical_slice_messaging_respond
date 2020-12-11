package edu.kit.ipd.sdq.respond.repository.`interface`

import com.google.gson.Gson
import edu.kit.ipd.sdq.respond.mqtt.PathMatcher
import edu.kit.ipd.sdq.respond.repository.*
import edu.kit.ipd.sdq.respond.repository.tables.Plant
import edu.kit.ipd.sdq.respond.repository.tables.Process
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*

class MqttRepositoryInterface(private val client: MqttClient, private val repository: Repository) : MqttCallback {
    private val gson = Gson()
    private val prefixRegex = Regex("(.*)/repository/.*")

    init {
        client.connect()
        client.setCallback(this)
        client.subscribe("#")
        // Publish existing processes at startup. Currently a bit inefficient on the database, but doesn't really
        // matter, as it's only run at startup
        repository.getPlants().forEach {plant ->
            publishProcesses(plant)
            repository.getProcesses(plant).forEach {processDescriptor ->
                repository.getProcess(processDescriptor.id, plant)?.let {
                    publishProcess(it, plant)
                }
            }
        }

    }

    private fun publishProcesses(plant: Plant) {
        val processes = repository.getProcesses(plant).map { it.asPayload }
        client.publish("${plant.path}/repository/processes", gson.toJson(processes).toMqttMessage(true))
    }

    private fun publishProcess(process: Process, plant: Plant) {
        val payload = process.asPayload
        client.publish("${plant.path}/repository/process/get/${process.uuid}", gson.toJson(payload).toMqttMessage(true))
    }

    private fun publishRemovedProcess(processId: ProcessId, plant: Plant) {
        client.publish("${plant.path}/repository/process/get/$processId", MqttMessage().also { it.isRetained = true })
    }

    private fun newProcess(mqttMessage: MqttMessage?, plant: Plant) {
        val payload = gson.fromJson(mqttMessage.toStringOrNull(), NewProcessPayload::class.java)
        val processContent = Process(payload.name, payload.process, plant)
        val process = repository.registerProcess(processContent)
        publishProcess(process, plant)
        publishProcesses(plant)
    }

    private fun deleteProcess(id: UUID?, plant: Plant) {
        if (id != null) {
            repository.removeProcess(id, plant)
            publishProcesses(plant)
            publishRemovedProcess(id, plant)
        }
    }

    private fun deleteAllProcesses(mqttMessage: MqttMessage?, plant: Plant) {
        val payload = mqttMessage.toStringOrNull()
        if (payload == "YES") {
            val processes = repository.getProcesses(plant)
            repository.removeAllProcesses(plant)
            publishProcesses(plant)
            processes.forEach {
                publishRemovedProcess(it.id, plant)
            }
        }
    }

    private fun updateProcess(id: UUID?, mqttMessage: MqttMessage?, plant: Plant) {
        val payload = mqttMessage.toStringOrNull()
        if (id != null && payload != null) {
            val process = repository.updateProcess(id, plant, payload)
            if (process != null) {
                publishProcess(process, plant)
            }
        }
    }

    private fun checkProcessCorrectness(id: UUID?, mqttMessage: MqttMessage?, plant: Plant) {
        if (id != null && mqttMessage != null) {
            val currentProcess = gson.fromJson(mqttMessage.toStringOrNull(), ProcessPayload::class.java)
            val actualProcess = repository.getProcess(id, plant)
            if (actualProcess == null) {
                publishRemovedProcess(id, plant)
                return
            }
            if (currentProcess != actualProcess.asPayload) {
                publishProcess(actualProcess, plant)
            }
        }
    }

    override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
        try {
            if (topic == null) return

            val prefixMatch = prefixRegex.matchEntire(topic) ?: return
            val plant = repository.getPlant(prefixMatch.groupValues[1]) ?: return

            val paths = PathMatcher(prefix = ".*/repository/") {
                "process/new" { newProcess(mqttMessage, plant) }
                "process/delete/([^/]+)" { deleteProcess(it[0].toUUIDOrNull(), plant) }
                "process/deleteAll" { deleteAllProcesses(mqttMessage, plant) }
                "update/([^/]+)" { updateProcess(it[0].toUUIDOrNull(), mqttMessage, plant) }
                "process/get/([^/]+)" { checkProcessCorrectness(it[0].toUUIDOrNull(), mqttMessage, plant) }
                default {
                    print("Unknown endpoint: $topic")
                }
            }

            paths.match(topic)
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


fun String.toMqttMessage(retained: Boolean = false) = MqttMessage(this.toByteArray()).also { it.isRetained = retained }
fun Any.toMqttMessage(retained: Boolean = false) = this.toString().toMqttMessage(retained)
fun MqttMessage?.toStringOrNull(): String? = this?.payload?.decodeToString()
fun MqttMessage?.toIntOrNull(): Int? = this.toStringOrNull()?.toIntOrNull()
fun String.toUUIDOrNull(): UUID? {
    return try {
        UUID.fromString(this)
    }
    catch (e: IllegalArgumentException) {
        null
    }
}