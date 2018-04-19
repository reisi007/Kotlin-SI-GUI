package at.reisisoft.sigui.commons.downloads

import at.reisisoft.and
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Predicate


enum class LibreOfficeDownloadFileType {
    MAIN, HP, SDK;

    override fun toString(): String {
        return super.toString().toLowerCase()
    }

    companion object {
        @JvmStatic
        fun fromFilename(fileName: String, downlaodType: DownloadType) =
            fromFilename(Paths.get(fileName), downlaodType)

        @JvmStatic
        fun fromFilename(fileName: Path, downlaodType: DownloadType): LibreOfficeDownloadFileType =
            fileName.fileName.toString().let { fileNameAsString ->
                when {
                    getPredicateFor(
                        HP,
                        downlaodType,
                        "."
                    ).test(fileNameAsString) -> HP // HP langage "." is just one char in REGEX!
                    getPredicateFor(SDK, downlaodType).test(fileNameAsString) -> SDK
                    else -> MAIN
                }
            }


        private val regexPart1 = ".*?(l|L)ib.+"
        private val regexPart2 = "(?<!\\.asc)$"
        private val regexForSdk = "((sdk|SDK).*?)"
        private fun regexForHp(lang: String) = "(hel.+_$lang.*?)"
        private val mainRegex by lazy { Regex(regexPart1 + regexPart2) }
        private val sdkRegex by lazy { Regex(regexPart1 + regexForSdk + regexPart2) }
        private val hpRegexMap: MutableMap<String, Regex> = HashMap()
        private fun getHPRegex(hpLang: String): Regex =
            hpRegexMap.computeIfAbsent(hpLang, {
                Regex(
                    regexPart1 + regexForHp(
                        it
                    ) + regexPart2
                )
            })


        private fun mustContainString(downlaodType: DownloadType): String = when (downlaodType) {
            DownloadType.MAC -> "mac"
            DownloadType.WINDOWSEXE, DownloadType.WINDOWS32, DownloadType.WINDOWS64 -> "win"
            DownloadType.LINUX_DEB_32, DownloadType.LINUX_DEB_64 -> "deb"
            DownloadType.LINUX_RPM_32, DownloadType.LINUX_RPM_64 -> "rpm"
            else -> throw IllegalStateException("Must contain string is not defined for $downlaodType")
        }


        @JvmStatic
        fun getPredicateFor(
            downloadFileType: LibreOfficeDownloadFileType,
            downlaodType: DownloadType,
            helppackLanguage: String = "."
        ): Predicate<String> {
            val mustContain = mustContainString(downlaodType)
            val predicate = Predicate<String> { !it.contains('/') && it.contains(mustContain, true) }
            return predicate and when (downloadFileType) {
                SDK -> Predicate { sdkRegex.matches(it) }
                HP -> getHPRegex(
                    helppackLanguage
                ).let { regex ->
                    Predicate<String> { regex.matches(it) }
                }
                MAIN -> {
                    val hp =
                        getHPRegex(".")//any HP
                    Predicate {
                        !it.contains("multi") && mainRegex.matches(it) && !hp.matches(it) && !sdkRegex.matches(it)
                    }
                }
            }
        }
    }
}