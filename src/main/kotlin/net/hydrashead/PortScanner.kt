package net.hydrashead

import picocli.CommandLine
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.util.concurrent.Callable


@CommandLine.Command(name="kotlin-port-scanner", version = ["0.1"], mixinStandardHelpOptions = true, description = ["A simple port scanner written in Kotlin"])
class PortScanner: Callable<Int> {

    @Parameters(index = "0", arity = "1", description = ["Host to scan, use IP, CIDR or DNS"])
    private lateinit var host: String

    @Option(names = ["-p", "--ports"], defaultValue = "-", description = ["Ports to scan. Separate by commas, use - to specify ranges. Eg: 80,443,8000-9000", "Will scan all ports by default"] )
    private var portArg: String = "-"

    @Option(names = ["-f", "--force"], defaultValue = "false", description = ["Force the scan regardless of ping status. Server ping may require root privileges."])
    private var force: Boolean = false

    override fun call(): Int {
        var ports: List<Int> = parsePortArg(portArg)
        println("Hello, World")
        return 0
    }

    private fun parsePortArg(portArg: String): List<Int> =
        portArg.split(",")
            .flatMap {
                when {
                    it.contains("-") -> parsePortRangeArg(it)
                    else -> listOf(it.toInt())
                }
            }

    private fun parsePortRangeArg(portRange: String): List<Int> {
        val range = portRange.split("-")
        val first = range[0].ifBlank { "0" }.toInt()
        val last = range[1].ifBlank { "65535" }.toInt()
        return (if(first < last) first..last else last..first).toList()
    }


}