package at.reisisoft.sigui.commons.downloads

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
        fun fromFilename(fileName: String) =
            fromFilename(Paths.get(fileName))

        @JvmStatic
        fun fromFilename(fileName: Path): LibreOfficeDownloadFileType =
            fileName.fileName.toString().let { fileNameAsString ->
                when {
                    getPredicateFor(
                        HP,
                        "."
                    ).test(fileNameAsString) -> HP // HP langage "." is just one char in REGEX!
                    getPredicateFor(SDK).test(fileNameAsString) -> SDK
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


        @JvmStatic
        fun getPredicateFor(
            downloadType: LibreOfficeDownloadFileType,
            helppackLanguage: String = "."
        ): Predicate<String> =
            when (downloadType) {
                SDK -> Predicate { !it.contains('/') && sdkRegex.matches(it) }
                HP -> getHPRegex(
                    helppackLanguage
                ).let { regex ->
                    Predicate<String> { !it.contains('/') && regex.matches(it) }
                }
                MAIN -> {
                    val hp =
                        getHPRegex(".")//any HP
                    Predicate {
                        !it.contains('/') && !it.contains("multi") && mainRegex.matches(it)
                                && !hp.matches(it) && !sdkRegex.matches(it)
                    }
                }

            }
    }
}