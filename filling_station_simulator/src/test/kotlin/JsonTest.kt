import edu.kit.ipd.sdq.respond.messaging.*
import java.util.*

@ExperimentalStdlibApi
fun main() {
    val senderUUID = UUID.randomUUID()!!
    val sensorUUID = UUID.randomUUID()!!
    val message = Message(
        listOf(
            SensorWeightEvent(
                100,
                senderUUID,
                sensorUUID
            )
        )
    )
    println(JsonCoding.encode(message).decodeToString())
    val message2 = Message(
        listOf(
            TaskStartEvent(
                0,
                0,
                DriveToStationTask(5)
            )
        )
    )
    println(JsonCoding.encode(message2).decodeToString())
}