package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.newLocaleTreeSet
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

internal object ResourceBundleUtils {
    private val DOWNLOADLIST = "downloadlist.named"
    val DOWNLOADLIST_NAMED = "$DOWNLOADLIST.special"
    val DOWNLOADLIST_FRESH = "$DOWNLOADLIST.fresh"
    val DOWNLOADLIST_ARCHIVE = "$DOWNLOADLIST.archive"
    val DOWNLOADLIST_STABLE = "$DOWNLOADLIST.stable"
    val DOWNLOADLIST_TESTING = "$DOWNLOADLIST.testing"
    val DOWNLOADLIST_DAILY = "$DOWNLOADLIST.daily"
    private val OPTIONS = "options"
    val OPTIONS_TITLE = "$OPTIONS.title"
    private val DOWNLAODER = "downloader"
    val DOWNLOADER_TITLE = "$DOWNLAODER.title"
    val DOWNLAODER_DOWNLOAD_FROM = "$DOWNLAODER.downloadfrom"
    val OPENFILE = "openfile"
    val MAIN_TITLE = "main.title"
    val CANCEL = "cancel"

    internal fun getSupportedLanguages(): Set<Locale> =
        newLocaleTreeSet().apply { add(Locale.forLanguageTag(EN_US)) }.apply {
            ResourceBundleUtils::class.java.classLoader.getResource("uistrings/sigui-desktop.properties").let {
                Paths.get(it.toURI())
            }.let {
                it.parent.let { uiResourcePath ->
                    Files.list(uiResourcePath).skip(1).map {
                        it.fileName.toString().let {
                            it.substring(it.indexOf('_') + 1, it.lastIndexOf('.'))
                        }
                    }.map { Locale.forLanguageTag(it) }.forEach { add(it) }
                }
            }
        }
}

internal const val EN_US = "en-US"