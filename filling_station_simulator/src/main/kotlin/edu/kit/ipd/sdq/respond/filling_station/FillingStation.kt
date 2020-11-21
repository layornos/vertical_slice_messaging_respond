package edu.kit.ipd.sdq.respond.filling_station

import edu.kit.ipd.sdq.respond.messaging.DriveToStationTask
import edu.kit.ipd.sdq.respond.messaging.TaskCompleteEvent
import edu.kit.ipd.sdq.respond.messaging.TaskStartEvent
import edu.kit.ipd.sdq.respond.messaging.MessagingClient
import edu.kit.ipd.sdq.respond.utils.listOfLambda
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.kodein.di.newInstance
import java.util.UUID
import kotlin.math.abs

class FillingStation(val client: MessagingClient, private val slider: Slider, private val scale: Scale, private val pumps: List<Pump>, val uuid: UUID = UUID.randomUUID()) {
    init {
        slider.parent = this
        scale.parent = this
        pumps.forEach {
            it.parent = this
        }
    }
    private val numStations = pumps.size

    fun moveSliderTo(index: Int) {
        assert(index >= 0)
        assert(index < numStations)
        client.publish(TaskStartEvent(0, 0, DriveToStationTask(index)).toMessage())
        slider.moveToPosition(slider.movingRange * index / numStations)
        client.publish(TaskCompleteEvent(0, 0).toMessage())
    }

    fun activatePump(index: Int, amount: Gram) {
        assert(index >= 0)
        assert(index < numStations)
        pumps[index].pump(amount) {
            if (isSliderUnderPump(index)) {
                scale.weight += it
            }
        }
    }

    private fun isSliderUnderPump(index: Int) = abs(slider.position - slider.movingRange * index / numStations) < 10
}

fun constructFillingStation(kodein: Kodein): FillingStation {
    val station by kodein.newInstance {
        FillingStation(
            instance(),
            instance(),
            instance(),
            listOfLambda(10) {
                instance() //Explicit generic needed because of type stripping at compile time
            }
        )
    }
    return station
}
