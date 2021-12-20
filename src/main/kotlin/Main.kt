import net.hydrashead.PortScanner
import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(CommandLine(PortScanner()).execute(*args))
}