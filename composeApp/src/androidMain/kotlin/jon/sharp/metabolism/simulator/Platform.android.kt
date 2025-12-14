package jon.sharp.metabolism.simulator

import android.os.Build

class AndroidPlatform : IPlatform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): IPlatform = AndroidPlatform()