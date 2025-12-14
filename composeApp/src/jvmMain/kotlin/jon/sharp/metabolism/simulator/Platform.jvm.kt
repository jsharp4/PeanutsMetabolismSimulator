package jon.sharp.metabolism.simulator

class JVMPlatform: IPlatform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): IPlatform = JVMPlatform()