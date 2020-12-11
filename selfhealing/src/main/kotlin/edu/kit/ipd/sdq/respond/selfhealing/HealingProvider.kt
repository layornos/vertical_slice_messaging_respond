package edu.kit.ipd.sdq.respond.selfhealing

import edu.kit.ipd.sdq.respond.selfhealing.`interface`.*
import java.util.*

class HealingProvider {
    fun findSolution(failure: FailurePayload): HealingPayload {
        return defaultPayload(failure.task.id)
    }

    private fun defaultPayload(id: UUID) = HealingPayload(
        ProcessPayload(
            DefinitionIdPayload(UUID(0, 0), "1.0")
        ),
        TaskPayload(id)
    )
}