package edu.kit.ipd.sdq.respond.filling_station

import edu.kit.ipd.sdq.respond.messaging.SensorWeightEvent
import java.util.*
import kotlin.math.abs
import kotlin.math.min

abstract class Component {
    lateinit var parent: FillingStation
}

abstract class Slider: Component() {
    abstract val movingRange: Millimeter
    abstract var position: Millimeter
    abstract fun moveToPosition(targetPosition: Millimeter)
}

class SimulatedSlider : Slider() {
    override val movingRange = 2000
    override var position = 0

    override fun moveToPosition(targetPosition: Millimeter) {
        while (abs(targetPosition - position) > 10) {
            position += 100 * if (targetPosition < position) -1 else 1
            Thread.sleep(500)
        }
    }
}

abstract class Scale: Component() {
    val topic = "Node/scale/Event/weight"
    val sensorUUID = UUID.randomUUID()
    abstract var weight: Gram
}

class SimulatedScale : Scale() {
    override var weight: Gram = 0
        set(value) {
            field = value
            val message = SensorWeightEvent(value, parent.uuid, sensorUUID).toMessage()
            parent.client.publish(message)
        }
}

abstract class Pump: Component() {
    val sensorUUID = UUID.randomUUID()
    abstract var content: Gram
    abstract fun pump(amount: Gram, callback: (Gram) -> Unit)
}

class SimulatedPump : Pump() {
    val topic = "Node/pump/Event/event"
    override var content = 1000
    override fun pump(amount: Gram, callback: (Gram) -> Unit) {
        var amountLeft = amount
        while (amountLeft > 0 && content > 0) {
            val toPump = min(content, min(amountLeft, 100))
            amountLeft -= toPump
            content -= toPump
            val message = SensorWeightEvent(content, parent.uuid, sensorUUID).toMessage()
            parent.client.publish(message)
            callback(toPump)
            Thread.sleep(500)
        }
    }
}
