import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

val mqttKodein = Kodein.Module("MQTT client", false) {
    bind<MqttAsyncClient>() with singleton {
        MqttAsyncClient(
            "tcp://127.0.0.1",
            "filling_station"
        ).also { it.connect() }
    }
}

val normalScenario = Kodein {
    import(mqttKodein)
    bind<Slider>() with provider { SimulatedSlider(instance()) }
    bind<Scale>() with provider { SimulatedScale(instance()) }
}
