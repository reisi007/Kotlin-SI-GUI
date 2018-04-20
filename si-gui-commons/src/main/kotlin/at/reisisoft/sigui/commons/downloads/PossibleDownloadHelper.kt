package at.reisisoft.sigui.commons.downloads

import at.reisisoft.*
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.SocketTimeoutException
import java.util.*
import java.util.function.*
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Collectors
import kotlin.Comparator
import kotlin.collections.ArrayList

object PossibleDownloadHelper {

    private val ALL_DOWNLOAD_LOCATIONS by lazy { DownloadLocation.values() }

    fun fetchPossibleFor(downloadTypes: List<DownloadType>): Map<DownloadLocation, Set<DownloadInformation>> =
        fetchPossibleFor(ALL_DOWNLOAD_LOCATIONS, downloadTypes, true)

    private fun fetchPossibleFor(
        downloadLocations: Array<DownloadLocation>,
        downloadTypes: List<DownloadType>,
        executeInParallel: Boolean = false
    ): Map<DownloadLocation, Set<DownloadInformation>> =
        TreeSet(downloadTypes).let {
            downloadLocations.stream().apply { if (executeInParallel) parallel() }
                .map { downloadLocation ->
                    when (downloadLocation) {
                        DownloadLocation.DAILY -> downloadLocation to possibleDailyDownloads(it)
                        DownloadLocation.STABLE -> downloadLocation to possibleReleaseVersion(
                            setOf(ReleaseType.STABLE, ReleaseType.FRESH),
                            it
                        )
                        DownloadLocation.ARCHIVE -> downloadLocation to possibleArchive(it)
                        DownloadLocation.TESTING -> DownloadLocation.STABLE to possibleTestingVersion(it)
                        else -> throw IllegalStateException("Unexpected location $downloadLocation")
                    }
                }.toMap()
        }


    private const val firstDesktopArchiveVersion = "3.3.0.4/"
    private const val lastDesktopArchiveVersion = "latest/"
    private fun possibleArchiveDesktop(
        baseUrlDocument: Document,
        downloadTypes: Set<DownloadType>
    ): SortedSet<DownloadInformation> =
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
                                downloadVersionInfo.addAll(
                                    getCorrectBaseUrlForOS(
                                        baseUrlDocument.location(),
                                        versionInfo,
                                        ArrayList<DownloadType>().also {
                                            //Add Support information
                                            if (downloadTypes.contains(DownloadType.WINDOWSEXE) && versionInfo < "3.5")
                                                it.add(DownloadType.WINDOWSEXE)
                                            else if (downloadTypes.contains(DownloadType.WINDOWS32))
                                                it.add(DownloadType.WINDOWS32)

                                            if (downloadTypes.contains(DownloadType.WINDOWS64) && versionInfo >= "4.4.3.2")
                                                it.add(DownloadType.WINDOWS64)

                                            //Add all elements which have always been available
                                            arrayOf(
                                                DownloadType.LINUX_DEB_64,
                                                DownloadType.LINUX_DEB_32,
                                                DownloadType.LINUX_RPM_64,
                                                DownloadType.LINUX_RPM_32,
                                                DownloadType.MAC
                                            ).stream().filter(downloadTypes::contains).forEach { dt -> it.add(dt) }

                                        })
                                )
                            }
                        if (possibleVersionInformation == lastDesktopArchiveVersion)
                            return downloadVersionInfo
                    }
                }
            }
            return downloadVersionInfo
        }


    private fun possibleArchive(downloadTypes: Set<DownloadType>): SortedSet<DownloadInformation> =
        parseHtmlDocument(DownloadUrls.ARCHIVE).let { rootDocument ->
            val downloads = TreeSet<DownloadInformation>()
            if (downloadTypes.contains(DownloadType.ANDROID_LIBREOFFICE_ARM) || downloadTypes.contains(DownloadType.ANDROID_LIBREOFFICE_X86))
                downloads += possibleArchiveAndroid("loviewer", rootDocument)
            if (downloadTypes.contains(DownloadType.ANDROID_REMOTE))
                downloads += possibleArchiveAndroid("sdremote", rootDocument)
            downloads += possibleArchiveDesktop(rootDocument, downloadTypes)
            return@let downloads
        }


    private fun possibleArchiveAndroid(
        folderContainsString: String,
        baseUrlDocument: Document
    ): SortedSet<DownloadInformation> = baseUrlDocument.select("a[href~=$folderContainsString]").let {
        it.stream().flatMap {
            setOf(DownloadType.ANDROID_LIBREOFFICE_X86, DownloadType.ANDROID_LIBREOFFICE_ARM).stream().map { dlType ->
                it.attr("href").let {
                    DownloadInformation(
                        "${baseUrlDocument.location()}$it",
                        it.substring(0, it.length - 1),
                        dlType
                    )
                }
            }
        }.toSortedSet()
    }

    private fun possibleTestingVersion(downloadTypes: Set<DownloadType>): SortedSet<DownloadInformation> =
        parseHtmlDocument(DownloadUrls.TESTING).let { rootDoocument ->
            rootDoocument.select("table a[href~=\\.]").stream()
                .map { it.attr("href") }.filter { it.endsWith('/') }
                .flatMap { urlFragment ->
                    getCorrectBaseUrlForOS(
                        "${rootDoocument.location()}$urlFragment",
                        "TESTING ${urlFragment.let { it.substring(0, it.length - 1) }}",
                        TreeSet(downloadTypes).apply { remove(DownloadType.WINDOWSEXE) }
                    ).stream()
                }.toSortedSet()
        }

    private enum class ReleaseType { STABLE, FRESH }

    private fun possibleReleaseVersion(
        releases: Collection<ReleaseType>,
        downloadTypes: Set<DownloadType>
    ): SortedSet<DownloadInformation> = parseHtmlDocument(DownloadUrls.STABLE).let { rootDocument ->
        releases.stream().flatMap { release ->
            //Maximum is what we want. Not the quickest solution, but we have < 10 [most likely 2-4] entries
            val versionComperator: Comparator<String> =
                if (release == ReleaseType.FRESH)
                    Comparator.naturalOrder()
                else Comparator { thiz, other ->
                    val thizLastIndex = thiz.lastIndexOf('.')
                    val thizIntVal = thiz.substring(0, thizLastIndex)
                    val otherLastIndex = other.lastIndexOf('.')
                    val otherIntVal = other.substring(0, otherLastIndex)

                    thizIntVal.compareTo(otherIntVal).let comparing@{
                        if (it != 0)
                            return@comparing -it //5.1 is greater than 5.2
                        else {
                            val thizSecondPart = thiz.substring(thizLastIndex)
                            val otherSecondPart = other.substring(otherLastIndex)
                            return@comparing thizSecondPart.compareTo(otherSecondPart) // 5.2.4 is greater than 5.2.3
                        }
                    }
                }

            rootDocument.select("table a[href~=\\.]").stream()
                .map { it.attr("href") }
                .filter { !it.startsWith('/') && it.endsWith('/') }
                .max(versionComperator)
                .orElseThrow { throw IllegalStateException("Unable to find $release!") }.let { urlFragment ->
                    getCorrectBaseUrlForOS(
                        "${rootDocument.location()}$urlFragment",
                        release.toString(),
                        TreeSet(downloadTypes).apply { remove(DownloadType.WINDOWSEXE) }
                    ).stream()
                }
        }.toSortedSet()
    }


    private const val bit32 = "x86"
    private const val bit64 = "x86_64"
    private fun getCorrectBaseUrlForOS(
        baseUrlWithoutOS: String,
        displayName: String,
        downloadTypes: Collection<DownloadType>
    ): SortedSet<DownloadInformation> = downloadTypes.stream().map {
        setOf(it) to when (it) {
            DownloadType.LINUX_DEB_64 -> "deb/$bit64/"
            DownloadType.LINUX_RPM_64 -> "rpm/$bit64/"
            DownloadType.LINUX_DEB_32 -> "deb/$bit32/"
            DownloadType.LINUX_RPM_32 -> "rpm/$bit32/"
            DownloadType.MAC -> "mac/$bit64/"
            DownloadType.WINDOWS32, DownloadType.WINDOWSEXE -> "win/$bit32/"
            DownloadType.WINDOWS64 -> "win/$bit64/"
            else -> ""
        }
    }.flatMap { (downloadTypes, urlFragment) ->
        downloadTypes.stream()
            .map { downloadType ->
                DownloadInformation("$baseUrlWithoutOS$urlFragment", displayName, downloadType)
            }
    }.toSortedSet()

    private fun possibleDailyDownloads(wantedDailyBuilds: Set<DownloadType>): SortedSet<DownloadInformation> =
        parseHtmlDocument(DownloadUrls.DAILY).let { rootDocument ->
            rootDocument.select("a[href]").let { aElements ->
                Regex("(master|libreoffice.*?)/").let { firstLevelRegex ->
                    aElements.stream().parallel().map { it.attr("href") }.filter { firstLevelRegex.matches(it) }
                        .map { branchName ->
                            val level2URL = "${rootDocument.location()}$branchName"
                            branchName to parseHtmlDocument(level2URL)
                        }.checkpoint(false)
                        .flatMap { (branchName, level2Document) ->
                            //Build new URL
                            level2Document.select("a[href~=@]").let { thinderboxNames ->
                                thinderboxNames.stream().parallel().map { it.attr("href") }.flatMap { thinderboxName ->
                                    getDownloadTypesFromThinderboxName(thinderboxName).stream().map {
                                        DownloadInformation(
                                            level2Document.location() + thinderboxName + "current/",
                                            "${branchName.substring(
                                                0,
                                                branchName.length - 1
                                            )}-${thinderboxName.substring(
                                                0,
                                                thinderboxName.length - 1
                                            )}", it
                                        )
                                    }
                                }.filter {
                                    it.supportedDownloadType.let { supportedType ->
                                        supportedType != DownloadType.UNKNOWN && wantedDailyBuilds.contains(
                                            supportedType
                                        )
                                    }
                                }
                                    //We now know every possible Thinderbox location. Now check if the Thinderbox is useful.
                                    .filter { dlInfo ->
                                        try {
                                            parseHtmlDocument(dlInfo.baseUrl).select("a[href~=.]")
                                                .let { downloadableElements ->
                                                    return@filter downloadableElements.isNotEmpty()
                                                }
                                        } catch (e: HttpStatusException) {
                                            if (e.statusCode !in 200..299)
                                                return@filter false
                                            throw e
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
    private fun getDownloadTypesFromThinderboxName(thinderboxName: String): Set<DownloadType> =
        when {
            thinderboxName.contains(
                "win",
                true
            ) -> setOf(if (thinderboxName.contains("x86_64")) DownloadType.WINDOWS64 else DownloadType.WINDOWS32)

            thinderboxName.contains("android", true) -> setOf(
                when {
                    thinderboxName.contains("x86") -> DownloadType.ANDROID_LIBREOFFICE_X86
                    else -> DownloadType.ANDROID_LIBREOFFICE_ARM
                //No remote master
                }
            )

            thinderboxName.contains("macos", true) -> setOf(DownloadType.MAC)

            thinderboxName.contains("linux", true) -> {
                val rpm = thinderboxName.contains("rpm", true)
                val deb = thinderboxName.contains("deb", true)
                val x64bit = thinderboxName.contains("x86_64")
                if (x64bit)
                    TreeSet<DownloadType>().apply {
                        if (rpm)
                            add(DownloadType.LINUX_RPM_64)
                        if (deb)
                            add(DownloadType.LINUX_DEB_64)
                    }
                else TreeSet<DownloadType>().apply {
                    if (rpm)
                        add(DownloadType.LINUX_RPM_32)
                    if (deb)
                        add(DownloadType.LINUX_DEB_32)
                }
            }
            else -> {
                println("Cannot infer downloadtype for: $thinderboxName")
                setOf(DownloadType.UNKNOWN)
            }
        }

    fun getFinalDownloadUrls(
        baseUrl: String,
        downloadType: DownloadType,
        fileTypes: Set<LibreOfficeDownloadFileType>,
        hpLanguage: String
    ): Map<LibreOfficeDownloadFileType, String> =
        parseHtmlDocument(baseUrl).let { rootDocument ->
            if (fileTypes.contains(LibreOfficeDownloadFileType.HP) && hpLanguage == ".")
                throw IllegalStateException("Helppack language is needed!")
            val regexMap: Map<LibreOfficeDownloadFileType, Predicate<String>> =
                fileTypes.stream()
                    .map { it to LibreOfficeDownloadFileType.getPredicateFor(it, downloadType, hpLanguage) }
                    .collect(Collectors.toMap({ it.first }, { it.second }))
            rootDocument.select("a[href]:first-child").map { it.html() }
                .let { listOfFileNames ->
                    fileTypes.stream().map { it to listOfFileNames }
                        .flatMap { (key, value) ->
                            value.stream().map { newValue -> key to newValue }
                        }.filter { (type, testName) -> regexMap.getValue(type).test(testName) }
                        .collect(object :
                            Collector<Pair<LibreOfficeDownloadFileType, String>, MutableMap<LibreOfficeDownloadFileType, String>, Map<LibreOfficeDownloadFileType, String>> {
                            override fun characteristics(): Set<Collector.Characteristics> =
                                setOf(Collector.Characteristics.UNORDERED)

                            override fun supplier(): Supplier<MutableMap<LibreOfficeDownloadFileType, String>> =
                                Supplier { EnumMap<LibreOfficeDownloadFileType, String>(LibreOfficeDownloadFileType::class.java) }

                            override fun accumulator(): BiConsumer<MutableMap<LibreOfficeDownloadFileType, String>, Pair<LibreOfficeDownloadFileType, String>> =
                                BiConsumer { map, (k, v) ->
                                    map.putIfAbsent(k, v)
                                }

                            override fun combiner(): BinaryOperator<MutableMap<LibreOfficeDownloadFileType, String>> =
                                BinaryOperator { thiz, other ->
                                    other.entries.stream().forEach { (k, v) -> thiz.putIfAbsent(k, v) }
                                    thiz
                                }

                            override fun finisher(): Function<MutableMap<LibreOfficeDownloadFileType, String>, Map<LibreOfficeDownloadFileType, String>> =
                                Function {
                                    Collections.unmodifiableMap(it)
                                }

                        }
                        )
                }
        }


    val hpRegex by lazy { Regex("help.*?_(?<languageTag>[a-zA-Z]{1,3}(-[a-zA-Z]{1,3})?)") }

    fun getHelppackLanguages(): Set<Locale> = getLocaleTreeSet().also { set ->
        parseHtmlDocument(DownloadUrls.HP_ENDPOINT).select("a[href~=help]").stream().map { it.attr("href") }
            .map { hpRegex.find(it) }.filter(Objects::nonNull).map { it!!.groups["languageTag"]!!.value }
            .map { Locale.forLanguageTag(it) }.forEach { set.add(it) }
    }

    private fun parseHtmlDocument(urlAsString: String): Document = getJsoupResponse(urlAsString).parse()

    private fun getJsoupResponse(urlAsString: String): Connection.Response =
        try {
            Jsoup.connect(urlAsString).timeout(CONNECTION_TIMEOUT).execute()
        } catch (ste: SocketTimeoutException) {
            System.err.println("Exception for $urlAsString")
            System.err.println()
            throw ste
        }

    private const val CONNECTION_TIMEOUT = 10000//10 seconds

}

fun getLocaleTreeSet(): TreeSet<Locale> =
    TreeSet(Comparator<Locale> { o1, o2 -> comparing(o1.toLanguageTag(), o2.toLanguageTag()) { 0 } }
    )