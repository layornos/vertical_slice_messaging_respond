package edu.kit.ipd.sdq.respond.repository.`interface`

import com.google.gson.TypeAdapterFactory
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import edu.kit.ipd.sdq.respond.repository.ProcessContent
import edu.kit.ipd.sdq.respond.repository.ProcessDescriptor
import edu.kit.ipd.sdq.respond.repository.ProcessId
import java.util.*

val commandTypeFactory: TypeAdapterFactory = RuntimeTypeAdapterFactory
    .of(Command::class.java, "type")
    .registerSubtype(AddProcessCommand::class.java)
    .registerSubtype(AddProcessResponse::class.java)
    .registerSubtype(GetProcessCommand::class.java)
    .registerSubtype(GetProcessResponse::class.java)
    .registerSubtype(RemoveProcessCommand::class.java)
    .registerSubtype(ListProcessesCommand::class.java)
    .registerSubtype(ListProcessesResponse::class.java)
    .registerSubtype(RemoveAllProcessesCommand::class.java)
    .registerSubtype(UpdateProcessCommand::class.java)

sealed class Command

class AddProcessCommand(val process: ProcessContent, val processName: String, val uuid: UUID = UUID.randomUUID()): Command()

class AddProcessResponse(val processId: ProcessId, val uuid: UUID): Command()

class GetProcessCommand(val processId: ProcessId) : Command()

class GetProcessResponse(val process: ProcessContent) : Command()

class RemoveProcessCommand(val processId: ProcessId) : Command()

class RemoveAllProcessesCommand : Command()

class ListProcessesCommand : Command()

class ListProcessesResponse(val processes: List<ProcessDescriptor>) : Command()

class UpdateProcessCommand(val processId: ProcessId, val process: ProcessContent) : Command()
