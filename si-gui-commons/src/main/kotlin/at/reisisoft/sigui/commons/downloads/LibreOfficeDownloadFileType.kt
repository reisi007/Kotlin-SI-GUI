package at.reisisoft.sigui.commons.downloads

import java.util.*

typealias StringPredicate = (String) -> Boolean

enum class LibreOfficeDownloadFileType {
    MAIN, HP, SDK;

    override fun toString(): String {
        return super.toString().toLowerCase()
    }

    companion object {
        private fun mustContainString(downlaodType: DownloadType): String = when (downlaodType) {
            DownloadType.MAC -> "mac"
            DownloadType.WINDOWSEXE, DownloadType.WINDOWS32, DownloadType.WINDOWS64 -> "win"
            DownloadType.LINUX_DEB_32, DownloadType.LINUX_DEB_64 -> "deb"
            DownloadType.LINUX_RPM_32, DownloadType.LINUX_RPM_64 -> "rpm"
            else -> throw IllegalStateException("Must contain string is not defined for $downlaodType")
        }

        private val regexPart1 = ".*?(l|L)ib.+"
        private val regexPart2 = "(?<!\\.asc)$"
        private val regexForSdk = "((sdk|SDK).*?)"
        private fun regexForHp(lang: String) = "(hel.+_$lang.*?)"
        private val mainRegex by lazy { Regex(regexPart1 + regexPart2) }
        private val sdkRegex by lazy { Regex(regexPart1 + regexForSdk + regexPart2) }
        private val hpRegexMap: MutableMap<String, Regex> = HashMap()
        private fun getHPRegex(hpLang: String): Regex =
            hpRegexMap.let {
                it.getOrPut(hpLang, {
                    Regex(
                        regexPart1 + regexForHp(
                            hpLang
                        ) + regexPart2
                    )
                })
            }

        @JvmStatic
        fun getPredicateFor(
            downloadFileType: LibreOfficeDownloadFileType,
            downlaodType: DownloadType,
            helppackLanguage: String = "."
        ): StringPredicate =
            mustContainString(downlaodType).let { mustContain ->
                {
                    !it.contains('/') && it.contains(mustContain, true) && when (downloadFileType) {
                        SDK -> sdkRegex.matches(it)
                        HP -> getHPRegex(
                            helppackLanguage
                        ).matches(it)
                        MAIN -> {
                            val hp =
                                getHPRegex(".")//any HP
                            !it.contains("multi") && mainRegex.matches(it) && !hp.matches(it) && !sdkRegex.matches(it)
                        }
                    }
                }
            }
    }
}