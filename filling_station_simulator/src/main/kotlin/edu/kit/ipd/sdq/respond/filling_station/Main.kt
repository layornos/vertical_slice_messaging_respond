package edu.kit.ipd.sdq.respond.filling_station

import edu.kit.ipd.sdq.respond.messaging.MessagingClient
import edu.kit.ipd.sdq.respond.messaging.MqttMessagingClient
import edu.kit.ipd.sdq.respond.utils.listOfLambda
import org.eclipse.paho.client.mqttv3.MqttClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.newInstance

fun main() {
    val connection = MqttMessagingClient(MqttClient("tcp://localhost", "filling_station"))
    val kodein = Kodein {
        extend(normalScenario)
        bind<MessagingClient>() with instance(connection)
    }
    val station by kodein.newInstance {
        FillingStation(
            instance(),
            instance(),
            listOfLambda(10) {
                instance<Pump>()
            }
        )
    }

    station.moveSliderTo(5)
    station.activatePump(1, 400)
    station.activatePump(5, 1500)

    connection.disconnect()
}
