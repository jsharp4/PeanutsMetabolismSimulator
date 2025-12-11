package jon.sharp.metabolism.simulator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform