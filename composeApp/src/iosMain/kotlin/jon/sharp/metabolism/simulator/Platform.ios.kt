package jon.sharp.metabolism.simulator

import platform.UIKit.UIDevice

class IOSPlatform: IPlatform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): IPlatform = IOSPlatform()