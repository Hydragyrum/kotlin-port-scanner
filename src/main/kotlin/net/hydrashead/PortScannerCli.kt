package net.hydrashead

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.tongfei.progressbar.ProgressBar
import org.apache.commons.net.util.SubnetUtils
import picocli.CommandLine
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Callable


@CommandLine.Command(
    name = "kotlin-port-scanner",
    version = ["0.1"],
    mixinStandardHelpOptions = true,
    description = ["A simple port scanner written in Kotlin"]
)
class PortScannerCli : Callable<Int> {

    @Parameters(index = "0", arity = "1", description = ["Host to scan, use IP, CIDR or DNS"])
    private lateinit var host: String

    @Option(
        names = ["-p", "--ports"],
        defaultValue = "-",
        description = ["Ports to scan. Separate by commas, use - to specify ranges. Eg: 80,443,8000-9000", "Will scan all ports by default"]
    )
    private var portArg = "-"

    @Option(
        names = ["-f", "--force"],
        defaultValue = "false",
        description = ["Force the scan regardless of ping status. Server ping may require root privileges."]
    )
    private var force = false

    @Option(
        names = ["-t", "--timeout"],
        defaultValue = "1000",
        description = ["Timeout in ms. Default is 1000"]
    )
    private var timeout = 1000

    override fun call(): Int {
        val hosts = parseHostsArg(host)
        val ports = parsePortArg(portArg)
        val socketInetAddresses = hosts
            .filter {
                force || it.isReachable(timeout)
            }
            .flatMap {
                generateAddresses(it, ports)
            }
        runBlocking {
            val pb = ProgressBar("Ports Scanned", socketInetAddresses.size.toLong())
            socketInetAddresses
                .map {
                    async {
                        scan(it, timeout)
                    }
                }
                .map {
                    pb.step()
                    it.await()
                }
                .filter {
                    it.second
                }
                .map { it.first }
                .forEach {
                    println("$it :: ${Ansi.AUTO.string("@|bold,green OPEN|@")}")
                }
        }
        return 0
    }

    private suspend fun scan(address: InetSocketAddress, timeout: Int): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val socket = Socket()
        runCatching { socket.connect(address, timeout) }
            .map { "${address.hostString}:${address.port}" to true }
            .getOrElse { "${address.hostString}:${address.port}" to false }
    }

    private fun generateAddresses(host: InetAddress, ports: List<Int>): List<InetSocketAddress> =
        ports.map { port ->
            InetSocketAddress(host, port)
        }

    private fun parseHostsArg(hostArg: String): List<InetAddress> {
        return when {
            hostArg.isCIDR() -> getListFromCIDR(hostArg)
            else -> listOf(InetAddress.getByName(hostArg))
        }
    }

    private fun getListFromCIDR(hostArg: String): List<InetAddress> =
        SubnetUtils(hostArg).info
            .allAddresses
            .toList()
            .map { InetAddress.getByName(it) }

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
        return (if (first < last) first..last else last..first).toList()
    }

    private fun String.isCIDR(): Boolean =
        this.matches(Regex("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/(\\d{1,2})"))

}