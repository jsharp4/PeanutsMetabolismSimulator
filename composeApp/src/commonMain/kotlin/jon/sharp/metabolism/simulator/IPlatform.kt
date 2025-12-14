package jon.sharp.metabolism.simulator

interface IPlatform {
    val name: String
}

expect fun getPlatform(): IPlatform