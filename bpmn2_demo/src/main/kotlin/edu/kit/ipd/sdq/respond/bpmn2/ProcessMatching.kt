package edu.kit.ipd.sdq.respond.bpmn2

import edu.kit.ipd.sdq.respond.bpmn2.extensions.outgoingNodes
import edu.kit.ipd.sdq.respond.messaging.Task
import org.eclipse.bpmn2.FlowNode
import org.eclipse.bpmn2.InclusiveGateway
import org.eclipse.bpmn2.StartEvent
import org.eclipse.bpmn2.impl.TaskImpl
import java.lang.IllegalStateException
import org.eclipse.bpmn2.Task as BpmnTask

class Process(val startEvent: StartEvent, eventReceiver: EventReceiver): EventCallback{
    var state: FlowNode = startEvent

    init {
        traverse()
        eventReceiver.register(this)
    }

    override fun onTaskStart(task: Task) {

    }

    override fun onTaskComplete() {

    }

    fun traverse() {
        when (state) {
            is StartEvent, is InclusiveGateway -> {
                assert(state.outgoingNodes.size == 1)
                state = state.outgoingNodes.first()
                traverse()
            }
            is Task -> return
            else -> throw IllegalStateException()
        }
    }
}
