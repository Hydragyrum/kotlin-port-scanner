import net.hydrashead.PortScannerCli
import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(CommandLine(PortScannerCli()).execute(*args))
}