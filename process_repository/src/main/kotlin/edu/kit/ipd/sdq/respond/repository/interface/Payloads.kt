package edu.kit.ipd.sdq.respond.repository.`interface`

import edu.kit.ipd.sdq.respond.repository.ProcessContent
import edu.kit.ipd.sdq.respond.repository.tables.ProcessDescriptor

interface Payload

class NewProcessPayload(val process: ProcessContent, val name: String) : Payload

class ProcessesPayload(val processes: List<ProcessDescriptor>) : Payload