package edu.kit.ipd.sdq.respond.repository.`interface`

import edu.kit.ipd.sdq.respond.repository.ProcessContent
import edu.kit.ipd.sdq.respond.repository.ProcessDescriptor
import edu.kit.ipd.sdq.respond.repository.tables.Process
import java.util.*

interface Payload

class NewProcessPayload(val process: ProcessContent, val name: String) : Payload

class ProcessesPayload(val processes: List<ProcessDescriptorPayload>) : Payload

class ProcessPayload(val name: String, val source: String, val definitionId: UUID)

class ProcessDescriptorPayload(val name: String, val definitionId: Int)

val ProcessDescriptor.asPayload: ProcessDescriptorPayload
    get() = ProcessDescriptorPayload(this.name, this.id)

val Process.asPayload: ProcessPayload
    get() = ProcessPayload(this.name, this.source, this.uuid)