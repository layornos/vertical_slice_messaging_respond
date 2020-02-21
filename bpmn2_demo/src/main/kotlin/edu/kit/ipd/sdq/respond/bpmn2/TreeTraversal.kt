package edu.kit.ipd.sdq.respond.bpmn2

import org.eclipse.bpmn2.*

fun DocumentRoot.findStartEvents() : List<StartEvent> {
    val list = mutableListOf<StartEvent>()
    this.definitions.rootElements.forEach { it.findStartEvents(list) }
    return list
}

fun RootElement.findStartEvents(list: MutableList<StartEvent>) : List<StartEvent> {
    if (this is Process) {
        this.flowElements.forEach { it.findStartEvents(list) }
    }
    return list
}

fun FlowElement.findStartEvents(list: MutableList<StartEvent>) : List<StartEvent> {
    if (this is StartEvent) {
        list.add(this)
    }
    return list
}

val FlowNode.outgoingNodes: List<FlowNode>
    get() = this.outgoing.map { it.targetRef }

val FlowNode.incomingNodes: List<FlowNode>
    get() = this.incoming.map { it.sourceRef}

fun FlowNode.traverse(callback: (FlowNode) -> Unit)  {
    callback(this)
    this.outgoingNodes.forEach { it.traverse(callback) }
}