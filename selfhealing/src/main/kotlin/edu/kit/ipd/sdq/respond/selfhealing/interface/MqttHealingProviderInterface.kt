package edu.kit.ipd.sdq.respond.selfhealing.`interface`

import com.google.gson.Gson
import edu.kit.ipd.sdq.respond.selfhealing.HealingProvider
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception
import edu.kit.ipd.sdq.respond.mqtt.PathMatcher

class MqttHealingProviderInterface(private val client: MqttClient, private val healingProvider: HealingProvider) : MqttCallback {
    private val gson = Gson()
    private val endpointRegex = Regex("(.*)/respond/failure")

    init {
        client.connect()
        client.setCallback(this)
        client.subscribe("#")
    }

    fun onFailure(mqttMessage: MqttMessage?, plant: String) {
        val payload = gson.fromJson(mqttMessage.toStringOrNull(), FailurePayload::class.java)
        val solution = healingProvider.findSolution(payload)

        client.publish("$plant/respond/healing", gson.toJson(solution).toMqttMessage())
    }


    override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
        try {
            if (topic == null) return

            val prefixMatch = endpointRegex.matchEntire(topic) ?: return
            val plant = prefixMatch.groupValues[1]

            onFailure(mqttMessage, plant)

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
