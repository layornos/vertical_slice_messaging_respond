package edu.kit.ipd.sdq.respond.bpmn2

import edu.kit.ipd.sdq.respond.bpmn2.extensions.findStartEvents
import edu.kit.ipd.sdq.respond.bpmn2.extensions.save
import edu.kit.ipd.sdq.respond.bpmn2.extensions.traverse
import org.eclipse.bpmn2.DocumentRoot
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.paho.client.mqttv3.MqttClient
import java.io.File
import java.io.FileOutputStream

fun main() {
    val rs = ResourceSetImpl()
    rs.resourceFactoryRegistry.extensionToFactoryMap["bpmn"] = Bpmn2ResourceFactoryImpl()
    rs.resourceFactoryRegistry.extensionToFactoryMap["bpmn2"] = Bpmn2ResourceFactoryImpl()

    val uri = URI.createFileURI(File(::main.javaClass.classLoader.getResource("scenario_4.bpmn2")!!.file).absolutePath)
    val bpmnResource = rs.getResource(uri, true)
    val documentRoot = bpmnResource.contents[0] as DocumentRoot

    val startEvents = documentRoot.findStartEvents()

    val eventReceiver = MqttEventReceiver(MqttClient("tcp://localhost", "matcher"))
    val process = Process(startEvents.first(), eventReceiver)
}
