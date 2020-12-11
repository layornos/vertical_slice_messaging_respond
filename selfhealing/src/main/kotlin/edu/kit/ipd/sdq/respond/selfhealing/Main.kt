package edu.kit.ipd.sdq.respond.selfhealing

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import edu.kit.ipd.sdq.respond.selfhealing.`interface`.MqttHealingProviderInterface
import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) = mainBody("Selfhealing") {
    val arguments = ArgParser(args).parseInto(::CommandLineArguments)

    val healingProvider = HealingProvider()
    val `interface` = MqttHealingProviderInterface(MqttClient(arguments.broker, MqttClient.generateClientId()), healingProvider)
}

class CommandLineArguments(parser: ArgParser) {
    val broker by parser.storing("--broker", "-b", help = "Url of the mqtt broker to use").default {
        getEnvOrNull("RESPOND_SELFHEALING_BROKER") ?: "tcp://localhost"
    }
}

fun getEnvOrNull(name: String): String? {
    val env = System.getenv()
    return if (env.containsKey(name)) env[name] else null
}
