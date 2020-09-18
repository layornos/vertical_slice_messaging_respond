package edu.kit.ipd.sdq.respond.repository.client

import com.google.gson.GsonBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import edu.kit.ipd.sdq.respond.repository.client.Modes.*
import edu.kit.ipd.sdq.respond.repository.`interface`.*
import org.eclipse.paho.client.mqttv3.MqttClient
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) = mainBody("RepositoryClient") {
    val arguments = ArgParser(args).parseInto(::ClientCommandLineArguments)

    val plant = if (arguments.plant.endsWith("/")) {
        arguments.plant
    } else {
        arguments.plant + "/"
    }

    val client = MqttClient(arguments.broker, "respond_repository_client").also { it.connect() }
    val gson = GsonBuilder().registerTypeAdapterFactory(commandTypeFactory).create()
    val request: Command
        request = when(arguments.mode) {
            SEND -> {
                val process = arguments.process ?: missingParameter("process")
                AddProcessCommand(File(process).readText(), process, UUID.randomUUID())
            }
            RECEIVE -> {
                val id = arguments.id ?: missingParameter("id")
                GetProcessCommand(id)
            }
            DELETE -> {
                val id = arguments.id ?: missingParameter("id")
                RemoveProcessCommand(id)
            }
            UPDATE -> {
                val id = arguments.id ?: missingParameter("id")
                val process = arguments.process ?: missingParameter("process")
                UpdateProcessCommand(id, process)
            }
            DELETE_ALL -> RemoveAllProcessesCommand()
            LIST -> ListProcessesCommand()
        }

    client.publish(plant + "repository", gson.toJson(request, Command::class.java).asMqttMessage())
    client.disconnect()
    client.close()
}

fun missingParameter(name: String): Nothing {
    System.err.println("Missing parameter '$name'")
    exitProcess(1)
}

