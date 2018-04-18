package at.reisisoft.sigui.commons.downloads;


enum class DownloadType {
    UNKNOWN, ANDROID_LIBREOFFICE_ARM, ANDROID_LIBREOFFICE_X86, ANDROID_REMOTE, WINDOWS32, WINDOWS64, WINDOWSEXE,
    MAC, LINUX_DEB_32, LINUX_DEB_64, LINUX_RPM_32, LINUX_RPM_64
}


enum class DownloadLocation {
    ARCHIVE, DAILY, STABLE, TESTING
}

internal object DownloadUrls {
    const val ARCHIVE = "https://downloadarchive.documentfoundation.org/libreoffice/old/"
    const val DAILY = "https://dev-builds.libreoffice.org/daily/"
    const val STABLE = "http://download.documentfoundation.org/libreoffice/stable/"
    const val TESTING = "http://download.documentfoundation.org/libreoffice/testing/"
}

private fun <T : Comparable<T>> comparing(thiz: T, other: T, ifUndecideable: () -> Int): Int =
    thiz.compareTo(other).let { result ->
        if (result != 0)
            return@let result
        return@let ifUndecideable()
    }

data class DownloadInformation(
    val baseUrl: String,
    val displayName: String,
    val supportedDownloadTypes: Set<DownloadType>
) :
    Comparable<DownloadInformation> {

    override fun compareTo(other: DownloadInformation): Int =
        comparing(displayName, other.displayName) {
            comparing(baseUrl, other.baseUrl) downloadTypes@{
                val thisIterator = supportedDownloadTypes.iterator()
                val otherIterator = other.supportedDownloadTypes.iterator()

                var thisHasNext = thisIterator.hasNext()
                var otherHasNext = otherIterator.hasNext()
                while (thisHasNext && otherHasNext) {
                    val thisE = thisIterator.next()
                    val otherE = otherIterator.next()
                    thisE.compareTo(otherE).let {
                        if (it != 0)
                            return@downloadTypes it
                    }
                    thisHasNext = thisIterator.hasNext()
                    otherHasNext = otherIterator.hasNext()
                }
                if (!thisHasNext && !otherHasNext)
                    return@downloadTypes 0
                if (thisHasNext && !otherHasNext)
                    return@downloadTypes 2
                else
                    return@downloadTypes -2
            }
        }
}