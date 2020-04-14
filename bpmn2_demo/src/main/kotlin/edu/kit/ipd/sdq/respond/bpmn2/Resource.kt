package edu.kit.ipd.sdq.respond.bpmn2

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.xmi.XMLResource
import java.io.OutputStream

fun Resource.save(stream: OutputStream) {
    this.save(stream, null)
}