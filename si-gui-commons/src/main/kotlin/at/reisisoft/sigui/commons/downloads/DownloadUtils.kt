package at.reisisoft.sigui.commons.downloads;


enum class DownloadType {
    UNKNOWN, ANDROID_LIBREOFFICE_ARM, ANDROID_LIBREOFFICE_X86, ANDROID_REMOTE, WINDOWS32, WINDOWS64, WINDOWSEXE
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
    val supportedDownloadTypes: Set<DownloadType>
) :
    Comparable<DownloadInformation> {

    override fun compareTo(other: DownloadInformation): Int {
        return baseUrl.compareTo(other.baseUrl).let {
            if (it != 0)
                it
            else
                displayName.compareTo(other.displayName).let {
                    if (it != 0)
                        it
                    else {
                        val thisIterator = supportedDownloadTypes.iterator()
                        val otherIterator = other.supportedDownloadTypes.iterator()

                        var thisHasNext = thisIterator.hasNext()
                        var otherHasNext = otherIterator.hasNext()
                        while (thisHasNext && otherHasNext) {
                            val thisE = thisIterator.next()
                            val otherE = otherIterator.next()
                            thisE.compareTo(otherE).let {
                                if (it != 0)
                                    return it
                            }
                            thisHasNext = thisIterator.hasNext()
                            otherHasNext = otherIterator.hasNext()
                        }
                        if (!thisHasNext && !otherHasNext)
                            return 0
                        if (thisHasNext && !otherHasNext)
                            return 2
                        else
                            return -2
                    }
                }
        }
    }

}