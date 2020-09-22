package edu.kit.ipd.sdq.respond.repository.client

import com.google.gson.Gson
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import edu.kit.ipd.sdq.respond.repository.client.Modes.*
import edu.kit.ipd.sdq.respond.repository.`interface`.*
import org.eclipse.paho.client.mqttv3.MqttClient
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = mainBody("RepositoryClient") {
    val arguments = ArgParser(args).parseInto(::ClientCommandLineArguments)

    val plant = if (arguments.plant.endsWith("/")) {
        arguments.plant
    } else {
        arguments.plant + "/"
    }

    val client = MqttClient(arguments.broker, "respond_repository_client").also { it.connect() }
    val gson = Gson()

    when (arguments.mode) {
        SEND -> {
            val processName = arguments.process ?: missingParameter("process")
            val payload = NewProcessPayload(File(processName).readText(), processName)
            client.publish("$plant/repository/new_process", gson.toJson(payload).toMqttMessage())
        }
        DELETE -> {
            val payload = arguments.id ?: missingParameter("id")
            client.publish("$plant/repository/delete_process", payload.toMqttMessage())
        }
        UPDATE -> {
            val id = arguments.id ?: missingParameter("id")
            val processName = arguments.process ?: missingParameter("process")
            val payload = File(processName).readText()
            client.publish("$plant/repository/update/$id", payload.toMqttMessage())
        }
        DELETE_ALL -> {
            val payload = "YES"
            client.publish("$plant/repository/delete_all_processes", payload.toMqttMessage())
        }
    }

    client.disconnect()
    client.close()
}

fun missingParameter(name: String): Nothing {
    System.err.println("Missing parameter '$name'")
    exitProcess(1)
}

