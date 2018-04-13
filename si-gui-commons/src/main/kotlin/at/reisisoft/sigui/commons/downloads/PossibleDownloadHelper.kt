package at.reisisoft.sigui.commons.downloads

import at.reisisoft.stream
import at.reisisoft.toSortedSet
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import java.util.*
import kotlin.collections.HashSet

object PossibleDownloadHelper {

    fun fetchPossibleFor(
        downloadLocation: DownloadLocation,
        vararg downloadTypes: DownloadType
    ): SortedSet<DownloadInformation> = downloadTypes.stream().flatMap { downloadType ->
        when (downloadLocation) {
            DownloadLocation.DAILY -> possibleDailyDownloads()
            DownloadLocation.ARCHIVE -> possibleArchive(downloadType)
            DownloadLocation.STABLE -> possibleReleaseVersion(ReleaseType.STABLE)
            DownloadLocation.FRESH -> possibleReleaseVersion(ReleaseType.FRESH)
        }.stream()
    }.toSortedSet()

    private const val firstDesktopArchiveVersion = "3.3.0.4/"
    private const val lastDesktopArchiveVersion = "latest/"
    private fun possibleArchiveDesktop(baseUrlDocument: Document): SortedSet<DownloadInformation> =
        baseUrlDocument.select("a[href]").let { elements ->
            val downloadVersionInfo = TreeSet<DownloadInformation>()
            var firstVersionSeen = false
            for (e in elements) {
                e.attr("href").let { possibleVersionInformation ->
                    if (possibleVersionInformation == firstDesktopArchiveVersion)
                        firstVersionSeen = true;

                    if (firstVersionSeen) {
                        possibleVersionInformation.substring(0, possibleVersionInformation.length - 1)
                            .let { versionInfo ->
                                downloadVersionInfo.add(
                                    DownloadInformation(
                                        "${baseUrlDocument.location()}$possibleVersionInformation",
                                        versionInfo,
                                        HashSet<DownloadType>().apply {
                                            //Add Support information
                                            if (versionInfo < "3.5")
                                                add(DownloadType.WINDOWSEXE)
                                            else
                                                add(DownloadType.WINDOWS32)

                                            if (versionInfo >= "4.4.3.2")
                                                add(DownloadType.WINDOWS64)
                                        }
                                    )
                                )
                            }
                        if (possibleVersionInformation == lastDesktopArchiveVersion)
                            return downloadVersionInfo
                    }
                }
            }
            return downloadVersionInfo
        }


    private fun possibleArchive(downloadType: DownloadType): SortedSet<DownloadInformation> =
        parseHtmlDocument(DownloadUrls.ARCHIVE).let { rootDocument ->
            return when (downloadType) {
                DownloadType.ANDROID_LIBREOFFICE_ARM, DownloadType.ANDROID_LIBREOFFICE_X86 -> possibleArchiveAndroid(
                    downloadType,
                    "loviewer",
                    rootDocument
                )
                DownloadType.ANDROID_REMOTE -> possibleArchiveAndroid(downloadType, "sdremote", rootDocument)
                else -> possibleArchiveDesktop(rootDocument)
            }
        }

    private fun possibleArchiveAndroid(
        downloadType: DownloadType,
        folderContainsString: String,
        baseUrlDocument: Document
    ): SortedSet<DownloadInformation> = baseUrlDocument.select("a[href~=$folderContainsString]").let {
        it.stream().map {
            it.attr("href").let {
                DownloadInformation(
                    "${baseUrlDocument.location()}$it",
                    it.substring(0, it.length - 1), mutableSetOf(downloadType)
                )
            }
        }.toSortedSet()
    }

    private enum class ReleaseType { STABLE, FRESH }

    private fun possibleReleaseVersion(release: ReleaseType): SortedSet<DownloadInformation> {
        TODO("Not implemented")
    }

    private fun possibleDailyDownloads(): SortedSet<DownloadInformation> =
        parseHtmlDocument(DownloadUrls.DAILY).let { rootDocument ->
            rootDocument.select("a[href]").let { aElements ->
                Regex("(master|libreoffice.*?)/").let { firstLevelRegex ->
                    aElements.stream().map { it.attr("href") }.filter { firstLevelRegex.matches(it) }
                        .flatMap { branchName ->
                            //Build new URL
                            val level2URL = "${rootDocument.location()}$branchName"
                            parseHtmlDocument(level2URL).let { level2Document ->
                                level2Document.select("a[href~=@]").let { thinderboxNames ->
                                    thinderboxNames.stream().map { it.attr("href") }.map { thinderboxName ->
                                        DownloadInformation(
                                            level2URL + thinderboxName + "current/",
                                            "${branchName.substring(
                                                0,
                                                branchName.length - 1
                                            )}-${thinderboxName.substring(
                                                0,
                                                thinderboxName.length - 1
                                            )}", getDownloadTypeFromThinderboxName(thinderboxName)

                                        )
                                    }.filter { !it.supportedDownloadTypes.contains(DownloadType.UNKNOWN) }
                                        //We now know every possible Thinderbox location. Now check if the Thinderbox is useful.
                                        .filter { dlInfo ->
                                            try {
                                                Jsoup.connect(dlInfo.baseUrl).timeout(CONNECTION_TIMEOUT)
                                                    .execute().let letResponse@{ response ->

                                                        Jsoup.parse(response.body()).select("a[href~=.]")
                                                            .let { downloadableElements ->
                                                                return@letResponse downloadableElements.isNotEmpty()
                                                            }

                                                    }
                                            } catch (e: HttpStatusException) {
                                                if (e.statusCode !in 200..299)
                                                    return@filter false
                                                throw e
                                            }
                                        }
                                }
                            }
                        }.toSortedSet()
                }
            }
        }

    /**
     * Only Windows and Android are supported
     */
    private fun getDownloadTypeFromThinderboxName(thinderboxName: String): Set<DownloadType> = setOf(
        when {
            thinderboxName.contains(
                "win",
                true
            ) -> if (thinderboxName.contains("x86_64")) DownloadType.WINDOWS64 else DownloadType.WINDOWS32
            thinderboxName.contains("android", true) -> when {
                thinderboxName.contains("x86") -> DownloadType.ANDROID_LIBREOFFICE_X86
                else -> DownloadType.ANDROID_LIBREOFFICE_ARM
            //No remote master
            }
            else -> {
                println("Cannot infer downloadtype for: $thinderboxName")
                DownloadType.UNKNOWN
            }
        }
    )

    private fun parseHtmlDocument(urlAsString: String): Document =
        URL(urlAsString).let { url ->
            Jsoup.parse(url, CONNECTION_TIMEOUT)
        }

    private const val CONNECTION_TIMEOUT = 5000/*5 seconds*/

}