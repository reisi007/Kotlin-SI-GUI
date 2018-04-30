package at.reisisoft.sigui.android

import at.reisisoft.sigui.commons.downloads.DownloadInformation

internal data class DisplayAbleDownloadInformation(internal val downloadInformation: DownloadInformation) {
    override fun toString(): String {
        return downloadInformation.displayName
    }
}