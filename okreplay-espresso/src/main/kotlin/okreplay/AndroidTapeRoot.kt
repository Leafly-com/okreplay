package okreplay

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import java.io.File

/** Provides a directory for OkReplay to store its tapes in. */
open class AndroidTapeRoot2(val context: Context, private val assetManager: AssetManager, testName: String) : DefaultTapeRoot(getSdcardDir(context, testName)) {
  constructor(context: Context, klass: Class<*>) : this(context, AssetManager(context), klass.simpleName)

  private val assetsDirPrefix = "tapes/$testName"

  override fun readerFor(tapeFileName: String) =
  // Instead of reading from the sdcard, we'll read tapes from the instrumentation apk assets
      // directory instead.
      assetManager.open("$assetsDirPrefix/$tapeFileName")

  override fun tapeExists(tapeFileName: String): Boolean =
      assetManager.exists(assetsDirPrefix, tapeFileName) == true

  internal fun grantPermissionsIfNeeded() {
    val res = context.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE)
    if (res != PackageManager.PERMISSION_GRANTED) {
      throw RuntimeException(
          "We need WRITE_EXTERNAL_STORAGE permission for OkReplay. Please add `adbOptions { installOptions \"-g\" }` to your build.gradle file."
      )
    }
    root.mkdirs()
    if (!root.exists()) {
      throw RuntimeException(
          "Failed to create the directory for tapes. Is your sdcard directory read-only?"
      )
    }
    setWorldWriteable(root)
  }

  @SuppressLint("SetWorldWritable")
  private fun setWorldWriteable(dir: File) {
    // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's manually set this
    dir.setWritable(/* writeable = */true, /* ownerOnly = */ false)
  }

  companion object {
    private fun getSdcardDir(context: Context, type: String): File {
      if (!isExternalStorageWritable()) {
        throw RuntimeException("Unable to access external storage")
      }

      // ${context.packageName}/ not included because it's a parent by way of getExternalFilesDir
      val parent = File(context.getExternalFilesDir(null), "okreplay/tapes/")
      parent.mkdirs()
      return File(parent, type)
    }

    /* Checks if external storage is available for read and write */
    private fun isExternalStorageWritable(): Boolean {
      val state = Environment.getExternalStorageState()
      return Environment.MEDIA_MOUNTED == state
    }
  }
}
