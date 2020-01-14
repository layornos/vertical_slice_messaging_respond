package edu.kit.ipd.sdq.respond.filling_station

import kotlin.math.abs

class FillingStation(private val slider: Slider, private val scale: Scale, private val pumps: List<Pump>) {
    val numStations = pumps.size

    fun moveTo(index: Int) {
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