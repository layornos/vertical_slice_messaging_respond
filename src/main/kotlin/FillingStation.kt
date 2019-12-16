import kotlinx.coroutines.Job

class FillingStation(private val slider: Slider, private val scale: Scale) {
    companion object {
        val NUM_STATIONS = 10
    }

    fun moveTo(index: Int): Job {
        assert(index >= 0)
        assert(index < NUM_STATIONS)
        return slider.moveToPosition(slider.movingRange * index / NUM_STATIONS)
    }
}