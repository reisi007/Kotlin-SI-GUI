package at.reisisoft.sigui.commons.downloads

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import java.util.*
import kotlin.collections.HashSet

object PossibleDownloadHelper {

    fun fetchPossibleFor(
        downloadType: DownloadType,
        downloadLocation: DownloadLocation
    ): SortedSet<DownloadInformation> = when (downloadLocation) {
        DownloadLocation.DAILY -> TODO("Not implemented")
        DownloadLocation.ARCHIVE -> possibleArchive(downloadType)
        DownloadLocation.STABLE -> possibleReleaseVersion(ReleaseType.STABLE)
        DownloadLocation.FRESH -> possibleReleaseVersion(ReleaseType.FRESH)
    }

    private const val firstDesktopArchiveVersion = "3.3.0.4/"
    private const val lastDesktopArchiveVersion = "latest/"
    private fun possibleArchiveDesktop(): SortedSet<DownloadInformation> =
        parseHtmlDocument(DownloadUrls.ARCHIVE).let select@{ baseUrlDocument ->
            baseUrlDocument.select("a[href]").let { elements ->
                val downloadVersionInfo = TreeSet<DownloadInformation>()
                var firstVersionSeen = false
                for (e in elements) {
                    e.attr("href").let { possibleVersionInformation ->
                        if (possibleVersionInformation == firstDesktopArchiveVersion)
                            firstVersionSeen = true;

                        if (firstVersionSeen) {
                            downloadVersionInfo.add(
                                DownloadInformation(
                                    "${baseUrlDocument.location()}$possibleVersionInformation",
                                    possibleVersionInformation.substring(0, possibleVersionInformation.length - 1),
                                    HashSet()
                                ).also { (_, versionName, downloadInformationSet) ->
                                    //Add Support information
                                    if (versionName < "3.5")
                                        downloadInformationSet.add(DownloadType.WINDOWSEXE)
                                    else
                                        downloadInformationSet.add(DownloadType.WINDOWS32)

                                    if (versionName >= "4.4.3.2")
                                        downloadInformationSet.add(DownloadType.WINDOWS64)

                                }
                            )
                            if (possibleVersionInformation == lastDesktopArchiveVersion)
                                return@select downloadVersionInfo
                            else {

                            }

                        } else {
                        }

                    }
                }
                return@let downloadVersionInfo
            }
        }


    private fun possibleArchive(downloadType: DownloadType): SortedSet<DownloadInformation> = when (downloadType) {
        DownloadType.ANDROID_LIBREOFFICE, DownloadType.ANDROID_REMOTE -> TODO("Not implemented")
        else -> possibleArchiveDesktop()
    }

    private enum class ReleaseType { STABLE, FRESH }

    private fun possibleReleaseVersion(release: ReleaseType): SortedSet<DownloadInformation> {
        TODO("Not implemented")
    }

    private fun parseHtmlDocument(urlAsString: String): Document =
        URL(urlAsString).let { url ->
            Jsoup.parse(url, 5000/*5 seconds*/)
        }

}