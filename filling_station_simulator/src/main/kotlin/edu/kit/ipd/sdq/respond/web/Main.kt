package edu.kit.ipd.sdq.respond.web

import edu.kit.ipd.sdq.respond.filling_station.constructFillingStation
import edu.kit.ipd.sdq.respond.filling_station.normalScenario
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
    fun start(@RequestParam("target") target: String?, @RequestParam("topic") topic: String) {
        val target = target ?: "tcp://localhost" //Spring doesn't support kotlins default parameter values, so set it manually
        val connection = MqttMessagingClient(MqttClient(target, "filling_station"), topic)
        val kodein = Kodein {
            extend(normalScenario)
            bind<MessagingClient>() with instance(connection)
        }
        val station = constructFillingStation(kodein)

        GlobalScope.launch {
            station.moveSliderTo(5)
            station.activatePump(1, 400)
            station.activatePump(5, 1500)

            connection.disconnect()
        }
    }
}

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}