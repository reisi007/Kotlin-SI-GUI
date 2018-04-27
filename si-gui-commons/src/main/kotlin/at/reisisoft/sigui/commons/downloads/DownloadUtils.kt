package at.reisisoft.sigui.commons.downloads;

import at.reisisoft.comparing


enum class DownloadType {
    UNKNOWN, ANDROID_LIBREOFFICE_ARM, ANDROID_LIBREOFFICE_X86, ANDROID_REMOTE, WINDOWS32, WINDOWS64, WINDOWSEXE,
    MAC, LINUX_DEB_32, LINUX_DEB_64, LINUX_RPM_32, LINUX_RPM_64
}


enum class DownloadLocation {
    ARCHIVE, DAILY, STABLE, TESTING
}

object DownloadUrls {
    const val ARCHIVE = "https://downloadarchive.documentfoundation.org/libreoffice/old/"
    const val DAILY = "https://dev-builds.libreoffice.org/daily/"
    const val STABLE = "http://download.documentfoundation.org/libreoffice/stable/"
    const val TESTING = "http://download.documentfoundation.org/libreoffice/testing/"
    const val HP_ENDPOINT = ARCHIVE + "latest/win/x86_64/"
}

data class DownloadInformation(
    val baseUrl: String,
    val displayName: String,
    val supportedDownloadType: DownloadType
) :
    Comparable<DownloadInformation> {

    override fun compareTo(other: DownloadInformation): Int =
        comparing(displayName, other.displayName) {
            comparing(baseUrl, other.baseUrl) {
                comparing(supportedDownloadType, other.supportedDownloadType) { 0 }
            }
        }
}