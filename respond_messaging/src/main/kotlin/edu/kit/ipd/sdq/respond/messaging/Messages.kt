package edu.kit.ipd.sdq.respond.messaging

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import java.time.Instant
import java.util.*

val eventTypeFactory = RuntimeTypeAdapterFactory
    .of(Event::class.java, "type")
    .registerSubtype(SensorWeightEvent::class.java, SensorWeightEvent.TYPE)
    .registerSubtype(TaskStartEvent::class.java, TaskStartEvent.TYPE)
    .registerSubtype(TaskCompleteEvent::class.java, TaskCompleteEvent.TYPE)

val taskTypeFactory = RuntimeTypeAdapterFactory
    .of(Task::class.java, "type")
    .registerSubtype(DriveToStartPositionTask::class.java, DriveToStartPositionTask.TYPE)
    .registerSubtype(DriveToStationTask::class.java, DriveToStationTask.TYPE)
    .registerSubtype(FillTask::class.java, FillTask.TYPE)

class Message(val events: List<Event>, val uuid: UUID = UUID.randomUUID(), val timestamp: Long = Instant.now().toEpochMilli())

abstract class Event(val uuid: UUID = UUID.randomUUID(), val timestamp: Long = Instant.now().toEpochMilli()) {
    abstract val eventType: String //Might be removed as redundant later
    fun toMessage() = Message(listOf(this))
}

class SensorWeightEvent(val value: Int, val senderUUID: UUID, val sensorUUID: UUID) : Event() {
    companion object {
        const val TYPE = "message.EventSensorWeight"
    }
    override val eventType = TYPE
}

class TaskStartEvent(val processId: Int, val workItemId: Int, val task: Task) : Event() {
    companion object {
        const val TYPE = "message.EventTaskStart"
    }
    override val eventType = TYPE
}

class TaskCompleteEvent(val processId: Int, val workItemId: Int) : Event() {
    companion object {
        const val TYPE = "message.EventTaskComplete"
    }
    override val eventType = TYPE
}

abstract class Task {
    abstract val type: String
}

class DriveToStartPositionTask : Task() {
    companion object {
        const val TYPE = "message.carriage.TaskDriveToStartPosition"
    }
    override val type = TYPE
}

class DriveToStationTask(val station: Int) : Task() {
    companion object {
       const val TYPE = "message.carriage.TaskDriveToStation"
    }
    override val type = TYPE
}

class FillTask(val amout: Int) : Task() {
    companion object {
        const val TYPE = "message.pump.TaskFill"
    }
    override val type = TYPE
}
