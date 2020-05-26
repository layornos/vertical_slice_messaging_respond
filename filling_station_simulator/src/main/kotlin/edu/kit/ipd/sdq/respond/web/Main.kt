package edu.kit.ipd.sdq.respond.web

import edu.kit.ipd.sdq.respond.filling_station.constructFillingStation
import edu.kit.ipd.sdq.respond.filling_station.normalScenario
import edu.kit.ipd.sdq.respond.messaging.JsonCoding
import edu.kit.ipd.sdq.respond.messaging.MessageCoding
import edu.kit.ipd.sdq.respond.messaging.MessagingClient
import edu.kit.ipd.sdq.respond.messaging.MqttMessagingClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.MqttClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CommandController {
    @PostMapping("/start")
    fun start(@RequestParam("broker") broker: String?, @RequestParam("topic") topic: String): String {
        //Spring doesn't support kotlins default parameter values, so set it manually
        val target = broker ?: "tcp://82.165.18.31:1883"
        val connection = MqttMessagingClient(MqttClient(target, "filling_station"), JsonCoding, topic)
        val kodein = Kodein {
            extend(normalScenario)
            bind<MessagingClient>() with instance(connection)
            bind<MessageCoding>() with instance(JsonCoding)
        }
        val station = constructFillingStation(kodein)

        GlobalScope.launch {
            station.moveSliderTo(5)
            station.activatePump(1, 400)
            station.activatePump(5, 1500)

            connection.disconnect()
        }

        return "Success"
    }
}

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}