package edu.kit.ipd.sdq.respond.filling_station

import kotlinx.coroutines.Job

class FillingStation(private val slider: Slider, private val scale: Scale, private val pumps: List<Pump>) {
    val numStations = pumps.size

    fun moveTo(index: Int) {
        assert(index >= 0)
        assert(index < numStations)
        slider.moveToPosition(slider.movingRange * index / numStations)
    }
}