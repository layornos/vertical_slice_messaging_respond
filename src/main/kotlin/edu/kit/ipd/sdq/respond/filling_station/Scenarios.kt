package edu.kit.ipd.sdq.respond.filling_station

import edu.kit.ipd.sdq.respond.messaging.MessagingClient
import edu.kit.ipd.sdq.respond.messaging.MqttMessagingClient
import org.eclipse.paho.client.mqttv3.MqttClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

val mqttKodein = Kodein.Module("MQTT client", false) {
    bind<MessagingClient>() with singleton {
        MqttMessagingClient(
            MqttClient(
                "tcp://127.0.0.1",
                "filling_station"
            )
        )
    }
}

var numPumps = 0
val normalScenario = Kodein {
    import(mqttKodein)
    bind<Slider>() with provider {
        SimulatedSlider(instance(), "position")
    }
    bind<Scale>() with provider {
        SimulatedScale(instance(), "weight")
    }
    bind<Pump>() with provider {
        SimulatedPump(instance(), "filling${numPumps++}")
    }
}
