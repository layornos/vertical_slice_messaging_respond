package edu.kit.ipd.sdq.respond.filling_station

import org.intellij.lang.annotations.Language
import java.time.Instant
import java.util.*

class Message(val events: List<Event>, val uuid: UUID = UUID.randomUUID(), val timestamp: Instant = Instant.now()) {
    @Language("JSON")
    fun toJson(body: String): String =
        """
        {
          "messageUuid": "$uuid",
          "timestamp": ${timestamp.milli},
          "body": [
              ${events.map { it.toJson() }.fold("") { acc, s -> "$acc, $s" }}
          ]
        }
        """.trimIndent()
}

abstract class Event(val uuid: UUID = UUID.randomUUID(), val timestamp: Instant = Instant.now()) {
    abstract val type: String
    abstract fun toJson(): String
}

class SensorWeightEvent(val value: Int, val senderUUID: UUID, val sensorUUID: UUID) : Event() {
    override val type = "message.EventSensorWeight"

    override fun toJson(): String =
        """
        {
            "type": "$type",
            "sData": $value,
            "sensorUuid": "$sensorUUID",
            "uuid": "$uuid",
            "senderUuid": "$senderUUID",
            "eventType": "$type",
            "timestamp": ${timestamp.milli}
        }
        """.trimIndent()
}

class TaskStartEvent(val processId: Int, val workItemId: Int, val taskType: TaskType) : Event() {
    override val type = "message.EventTaskStart"

    override fun toJson() =
        """
        {
            "type": "$type",
            "processId": $processId,
            "workItemId": $workItemId,
            "task": {
                "type": "${taskType.id}"
            },
            "uuid": "$uuid",
            "eventType": "$type"
        }
        """.trimIndent()
}

class TaskCompleteEvent(val processId: Int, val workItemId: Int) : Event() {
    override val type = "message.EventTaskComplete"

    override fun toJson() =
        """
        {
            "type": "$type",
            "processId": $processId,
            "workItemId": $workItemId,
            "uuid": "$uuid",
            "eventType": "$type"
        }
        """.trimIndent()
}

enum class TaskType(val id: String) {
    DRIVE_TO_START_POSITION("message.carriage.TaskDriveToStartPosition"),
    DRIVE_TO_STATION("message.carriage.TaskDriveToStation"),
    FILL("message.pump.TaskFill"),
}

