import com.nhaarman.mockitokotlin2.*
import edu.kit.ipd.sdq.respond.repository.Repository
import edu.kit.ipd.sdq.respond.repository.`interface`.MqttRepositoryInterface
import edu.kit.ipd.sdq.respond.repository.`interface`.toMqttMessage
import edu.kit.ipd.sdq.respond.repository.tables.Plant
import edu.kit.ipd.sdq.respond.repository.tables.Process
import edu.kit.ipd.sdq.respond.repository.tables.ProcessDescriptor
import io.kotest.core.spec.style.ExpectSpec
import org.eclipse.paho.client.mqttv3.MqttClient

class MqttRepositoryInterfaceTest  : ExpectSpec({
    val plantName = "default"
    val processName = "testprocess"
    val processContent = "content"
    val processId = 123

    context("new_process") {
        expect("interface should add process to repository") {
            val plant = Plant(plantName)
            val process = Process(processName, processContent, plant)
            val client = mock<MqttClient>()
            val repository = mock<Repository> {
                on { getPlant(plantName) } doReturn plant
                on { registerProcess(any()) } doReturn process
            }
            val `interface` = MqttRepositoryInterface(client, repository)

            `interface`.messageArrived(
                "default/repository/new_process",
                "{process: \"$processContent\", name: \"$processName\"}".toMqttMessage()
            )

            verify(repository).registerProcess(process)
            verify(client).publish(eq("default/repository/processes"), any())
            verify(client).publish(eq("default/repository/process/0"), any())
        }
    }

    context("delete_process") {
        expect("interface should delete a process") {
            val plant = Plant(plantName)
            val client = mock<MqttClient>()
            val repository = mock<Repository> {
                on { getPlant(plantName) } doReturn plant
            }
            val `interface` = MqttRepositoryInterface(client, repository)

            `interface`.messageArrived("default/repository/delete_process", processId.toMqttMessage())

            verify(repository).removeProcess(processId, plant)
            verify(client).publish(eq("default/repository/processes"), any())
            verify(client).publish(eq("default/repository/process/123"), any())
        }
    }

    context("delete_all_processes") {
        expect("interface should delete all processes") {
            val plant = Plant(plantName)
            val client = mock<MqttClient>()
            val repository = mock<Repository> {
                on { getPlant(plantName) } doReturn plant
                on { getProcesses(plant) } doReturn listOf(ProcessDescriptor("test1", 1), ProcessDescriptor("test2", 2))
            }
            val `interface` = MqttRepositoryInterface(client, repository)

            `interface`.messageArrived("default/repository/delete_all_processes", "YES".toMqttMessage())

            verify(repository).removeAllProcesses(plant)
            verify(client).publish(eq("default/repository/processes"), any())
            verify(client).publish(eq("default/repository/process/1"), any())
            verify(client).publish(eq("default/repository/process/2"), any())
        }
    }

    context("update") {
        expect("interface should update a process") {
            val plant = Plant(plantName)
            val client = mock<MqttClient>()
            val repository = mock<Repository> {
                on { getPlant(plantName) } doReturn plant
                on { updateProcess(1, plant, processContent) } doReturn Process(processName, processContent, plant).also { it.id = 1 }
            }
            val `interface` = MqttRepositoryInterface(client, repository)

            `interface`.messageArrived("default/repository/update/1", processContent.toMqttMessage())

            verify(repository).updateProcess(1, plant, processContent)
            verify(client).publish(eq("default/repository/process/1"), any())
        }
    }
})
