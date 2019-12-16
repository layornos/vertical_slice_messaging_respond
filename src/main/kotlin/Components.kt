import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.abs

abstract class Slider(val client: MqttAsyncClient) {
    abstract val movingRange: Millimeter
    abstract var position: Millimeter
    abstract fun moveToPosition(targetPosition: Millimeter): Job
}

class SimulatedSlider(client: MqttAsyncClient) : Slider(client) {
    override val movingRange = 2000
    override var position = 0

    override fun moveToPosition(targetPosition: Millimeter) = GlobalScope.launch {
        while (abs(targetPosition - position) > 10) {
            position += 10 * if (targetPosition < position) -1 else 1
            client.publish("position", MqttMessage("$position".toByteArray()))
            delay(100);
        }
    }
}

abstract class Scale(val client: MqttAsyncClient) {
    abstract var weight: Gramm
}

class SimulatedScale(client: MqttAsyncClient) : Scale(client) {
    override var weight: Gramm = 0
        set(value) {
            field = value
            client.publish("weight", MqttMessage("$value".toByteArray()))
        }
}

abstract class Pump(val client: MqttAsyncClient) {

}
