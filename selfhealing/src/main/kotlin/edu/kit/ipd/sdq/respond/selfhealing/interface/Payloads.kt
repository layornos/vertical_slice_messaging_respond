package edu.kit.ipd.sdq.respond.selfhealing.`interface`

import java.util.*

interface Payload

class FailurePayload(val id: UUID, val definitionId: DefinitionIdPayload, val task: TaskPayload): Payload

class DefinitionIdPayload(val id: UUID, version: String): Payload

class TaskPayload(val id: UUID): Payload

class HealingPayload(val process: ProcessPayload, val fault: TaskPayload): Payload

class ProcessPayload(val definitionId: DefinitionIdPayload): Payload