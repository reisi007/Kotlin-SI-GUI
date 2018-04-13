package at.reisisoft.sigui.commons.downloads;


enum class DownloadType {
    ANDROID_LIBREOFFICE, ANDROID_REMOTE, WINDOWS32, WINDOWS64, WINDOWSEXE
}


enum class DownloadLocation {
    ARCHIVE, DAILY, FRESH, STABLE
}

internal object DownloadUrls {
    const val ARCHIVE = "https://downloadarchive.documentfoundation.org/libreoffice/old/"
    const val DAILY = "https://dev-builds.libreoffice.org/daily/"
}

data class DownloadInformation(
    val baseUrl: String,
    val displayName: String,
    val supportedDownloadTypes: MutableSet<DownloadType>
) :
    Comparable<DownloadInformation> {

    override fun compareTo(other: DownloadInformation): Int {
        return baseUrl.compareTo(other.baseUrl).let {
            if (it == 0)
                0
            else
                displayName.compareTo(other.displayName)
        }
    }

}