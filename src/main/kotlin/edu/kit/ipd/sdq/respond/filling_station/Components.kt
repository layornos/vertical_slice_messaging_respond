package edu.kit.ipd.sdq.respond.filling_station

import edu.kit.ipd.sdq.respond.messaging.MessagingClient
import kotlinx.coroutines.*
import kotlin.math.abs

abstract class Slider(val client: MessagingClient) {
    abstract val movingRange: Millimeter
    abstract var position: Millimeter
    abstract fun moveToPosition(targetPosition: Millimeter)
}

class SimulatedSlider(client: MessagingClient) : Slider(client) {
    override val movingRange = 2000
    override var position = 0

    override fun moveToPosition(targetPosition: Millimeter) {
        while (abs(targetPosition - position) > 10) {
            position += 100 * if (targetPosition < position) -1 else 1
            client.publish("position", "$position".toByteArray())
            Thread.sleep(100)
        }
    }
}

abstract class Scale(val client: MessagingClient) {
    abstract var weight: Gramm
}

class SimulatedScale(client: MessagingClient) : Scale(client) {
    override var weight: Gramm = 0
        set(value) {
            field = value
            client.publish("weight", "$value".toByteArray())
        }
}

abstract class Pump(val client: MessagingClient)

class SimulatedPump(client: MessagingClient) : Pump(client)
