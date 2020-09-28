package edu.kit.ipd.sdq.respond.repository

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import edu.kit.ipd.sdq.respond.repository.`interface`.MqttRepositoryInterface
import org.eclipse.paho.client.mqttv3.MqttClient
import org.hibernate.cfg.Configuration

fun main(args: Array<String>) = mainBody("Repository") {
    val arguments = ArgParser(args).parseInto(::CommandLineArguments)

    val configuration = Configuration().configure()
    configuration.apply {
        setProperty("hibernate.connection.url", arguments.database)
        setProperty("hibernate.connection.username", arguments.databaseUser)
        setProperty("hibernate.connection.password", arguments.databasePassword)
    }
    val repository = HibernateRepository(configuration.buildSessionFactory())
    if (arguments.createDemoPlants) {
        repository.addPlant("default")
        repository.addPlant("demo1")
        repository.addPlant("demo2")
    }
    val `interface` = MqttRepositoryInterface(MqttClient(arguments.broker, "respond_repository"), repository)
}

class CommandLineArguments(parser: ArgParser) {
    val broker by parser.storing("--broker", "-b", help = "Url of the mqtt broker to use").default {
        getEnvOrNull("RESPOND_REPOSITORY_BROKER") ?: "tcp://localhost"
    }
    val database by parser.storing("--database", "-d", help = "Url of the database to use").default {
        getEnvOrNull("RESPOND_REPOSITORY_DATABASE") ?: "jdbc:mysql://localhost/respond?useUnicode=yes&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
    }
    val databaseUser by parser.storing("--dbuser", "-u", help = "The username for connecting to the database").default {
        getEnvOrNull("RESPOND_REPOSITORY_DATABASE_USER") ?: "root"
    }
    val databasePassword by parser.storing("--dbpassword", "-p", help = "The password for connecting to the database").default {
        getEnvOrNull("RESPOND_REPOSITORY_DATABASE_PASSWORD") ?: ""
    }
    val createDemoPlants by parser.flagging("--demo", help = "Creates demo plants on startup").default {
        getEnvOrNull("RESPOND_REPOSITORY_CREATE_DEMO_PLANTS") == "1"
    }
}

fun getEnvOrNull(name: String): String? {
    val env = System.getenv()
    return if (env.containsKey(name)) env[name] else null
}