package okreplay

import com.android.annotations.VisibleForTesting
import org.gradle.api.logging.Logger
import java.io.File

internal class DeviceBridgeProvider {
  companion object {
    private var instance: DeviceBridge? = null

    internal fun get(adbPath: File, adbTimeoutMs: Int, logger: Logger): DeviceBridge =
        if (instance != null) {
          instance as DeviceBridge
        } else {
          DeviceBridge(adbPath, adbTimeoutMs, logger)
        }

    @VisibleForTesting internal fun setInstance(deviceBridge: DeviceBridge) {
      instance = deviceBridge
    }
  }
}
