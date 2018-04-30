package at.reisisoft.sigui.android

import android.os.Build
import at.reisisoft.sigui.commons.downloads.DownloadType

object AndroidUtils {

    fun getDownloadTypes(): Set<DownloadType> =
        Build.SUPPORTED_ABIS.asSequence().map {
            when {
                it.contains("arm", true) -> DownloadType.ANDROID_LIBREOFFICE_ARM
                it.contains("x86", true) -> DownloadType.ANDROID_LIBREOFFICE_X86
                else -> null
            }
        }.filterNotNull().plus(DownloadType.ANDROID_REMOTE).toSet()
}