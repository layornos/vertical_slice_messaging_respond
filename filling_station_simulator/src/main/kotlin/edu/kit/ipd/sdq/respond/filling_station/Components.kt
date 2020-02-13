package edu.kit.ipd.sdq.respond.filling_station

import edu.kit.ipd.sdq.respond.messaging.MessagingClient
import kotlin.math.abs
import kotlin.math.min

abstract class Slider(val client: MessagingClient) {
    abstract val movingRange: Millimeter
    abstract var position: Millimeter
    abstract fun moveToPosition(targetPosition: Millimeter)
}

class SimulatedSlider(client: MessagingClient, private val name: String) : Slider(client) {
    override val movingRange = 2000
    override var position = 0

    override fun moveToPosition(targetPosition: Millimeter) {
        while (abs(targetPosition - position) > 10) {
            position += 100 * if (targetPosition < position) -1 else 1
            client.publish(name, "$position".toByteArray())
            Thread.sleep(500)
        }
    }
}

abstract class Scale(val client: MessagingClient) {
    abstract var weight: Gram
}

class SimulatedScale(client: MessagingClient, private val name: String) : Scale(client) {
    override var weight: Gram = 0
        set(value) {
            field = value
            client.publish(name, "$value".toByteArray())
        }
}

abstract class Pump(val client: MessagingClient) {
    abstract var content: Gram
    abstract fun pump(amount: Gram, callback: (Gram) -> Unit)
}

class SimulatedPump(client: MessagingClient, private val name: String) : Pump(client) {
    override var content = 1000
    override fun pump(amount: Gram, callback: (Gram) -> Unit) {
        var amountLeft = amount
        while (amountLeft > 0 && content > 0) {
            val toPump = min(content, min(amountLeft, 100))
            amountLeft -= toPump
            content -= toPump
            client.publish(name, "$content".toByteArray())
            callback(toPump)
            Thread.sleep(500)
        }
    }
}
