package edu.kit.ipd.sdq.respond.messaging

import java.time.Instant
import java.util.*

class Message(val events: List<Event>, val uuid: UUID = UUID.randomUUID(), val timestamp: Long = Instant.now().toEpochMilli())

abstract class Event(val uuid: UUID = UUID.randomUUID(), val timestamp: Long = Instant.now().toEpochMilli()) {
    abstract val type: String
    abstract val eventType: String
    fun toMessage() = Message(listOf(this))
}

class SensorWeightEvent(val value: Int, val senderUUID: UUID, val sensorUUID: UUID) : Event() {
    override val type = "message.EventSensorWeight"
    override val eventType = type
}

class TaskStartEvent(val processId: Int, val workItemId: Int, val task: Task) : Event() {
    override val type = "message.EventTaskStart"
    override val eventType = type
}

class TaskCompleteEvent(val processId: Int, val workItemId: Int) : Event() {
    override val type = "message.EventTaskComplete"
    override val eventType = type
}

abstract class Task {
    abstract val type: String
}

class DriveToStartPositionTask : Task() {
    override val type = "message.carriage.TaskDriveToStartPosition"
}

class DriveToStationTask(val station: Int) : Task() {
    override val type = "message.carriage.TaskDriveToStation"
}

class FillTask(val amout: Int) : Task() {
    override val type = "message.pump.TaskFill"
}
