import org.kodein.di.generic.instance
import org.kodein.di.newInstance
import java.lang.Thread.sleep

suspend fun main() {
    val station by normalScenario.newInstance { FillingStation(instance(), instance()) }
    station.moveTo(5).join()
}
