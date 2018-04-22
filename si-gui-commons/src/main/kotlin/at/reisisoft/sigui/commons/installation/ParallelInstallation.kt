package at.reisisoft.sigui.commons.installation

import at.reisisoft.sigui.commons.downloads.DownloadType

object ParallelInstallation {

}

enum class ParallelInstallationOS {
    WINDOWS, LINUX_DEB, LINUX_RPM;

    companion object {
        @JvmStatic
        fun fromDownloadType(from: DownloadType): ParallelInstallationOS = when (from) {
            DownloadType.WINDOWS32, DownloadType.WINDOWS64 -> WINDOWS
            DownloadType.LINUX_RPM_32, DownloadType.LINUX_RPM_64 -> LINUX_RPM
            DownloadType.LINUX_DEB_32, DownloadType.LINUX_DEB_64 -> LINUX_DEB
            else -> throw DownloadTypeNotSupported(from)
        }
    }
}

class DownloadTypeNotSupported(val downloadType: DownloadType) :
    Exception("Parallel installation of \"$downloadType\" is not supported!")