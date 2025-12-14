package jon.sharp.metabolism.simulator

class WasmPlatform: IPlatform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): IPlatform = WasmPlatform()