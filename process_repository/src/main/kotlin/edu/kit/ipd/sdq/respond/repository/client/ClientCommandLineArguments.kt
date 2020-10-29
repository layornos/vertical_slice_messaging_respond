package edu.kit.ipd.sdq.respond.repository.client

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class ClientCommandLineArguments(parser: ArgParser) {
    /*val send by parser.storing("--send", "-s", help = "Send a process to the repository").default<String?>(null)
    val receive by parser.storing("--receive", "-r", help = "Retrieve a process from the repository") { toIntOrNull() }.default<Int?>(null)
    val delete by parser.storing("--delete", "-d", help = "Delete a process from the repository") { toIntOrNull() }.default<Int?>(null)
    val list by parser.flagging("--list", "-l", help = "List the processes in the repository")
    val update by parser.flagging("--update", "-u", help = "Update a process")*/
    val mode by parser.mapping(Modes.getArgMap(), help = "What mode to operate in")
    val broker by parser.storing("--broker", "-b", help = "Url of the mqtt broker to use").default("tcp://localhost")
    val plant by parser.storing("--plant", "-q", help = "Path of the plant to use").default("default")
    val process by parser.storing("--process", "-p", help = "A file representing a process").default { null }
    val id by parser.storing("--id", "-i", help = "The id of the process to operate on") { toIntOrNull() }.default { null }
}

enum class Modes(val longFlag: String, val shortFlag: String) {
    SEND("--send", "-s"),
    //RECEIVE("--receive", "-r"),
    DELETE("--delete", "-d"),
    UPDATE("--update", "-u"),
    DELETE_ALL("--deleteAll", "-a");
    //LIST("--list", "-l");

    companion object {
        fun getArgMap(): Map<String, Modes> {
            return values().associateBy { it.longFlag } + values().associateBy { it.shortFlag }
        }
    }
}