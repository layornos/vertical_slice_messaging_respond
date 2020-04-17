package edu.kit.ipd.sdq.respond.filling_station

import edu.kit.ipd.sdq.respond.utils.listOfLambda
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.kodein.di.newInstance
import kotlin.math.abs

class FillingStation(private val slider: Slider, private val scale: Scale, private val pumps: List<Pump>) {
    private val numStations = pumps.size

    fun moveSliderTo(index: Int) {
        assert(index >= 0)
        assert(index < numStations)
        slider.moveToPosition(slider.movingRange * index / numStations)
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
            listOfLambda(10) {
                instance<Pump>() //Explicit generic needed because of type stripping at compile time
            }
        )
    }
    return station
}
