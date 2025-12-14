package jon.sharp.metabolism.simulator

class JsPlatform: IPlatform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): IPlatform = JsPlatform()