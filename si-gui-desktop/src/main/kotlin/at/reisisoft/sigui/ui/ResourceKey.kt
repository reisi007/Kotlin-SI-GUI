package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.newLocaleTreeSet
import com.sun.nio.zipfs.JarFileSystemProvider
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private val DOWNLOADLIST = "downloadlist.named"
private val DOWNLAODER = "downloader"
private val ERROR = "error"
private val MANAGER = "manager"
private val LICENSE = "license"
private val MENU = "menu"
private val OPTIONS = "options"
private val ABOUT = "about"

internal enum class ResourceKey(private val resourceBundleKey: String) {
    APPNAME("appname"),

    DOWNLOADLIST_NAMED("$DOWNLOADLIST.special"),
    DOWNLOADLIST_FRESH("$DOWNLOADLIST.fresh"),
    DOWNLOADLIST_ARCHIVE("$DOWNLOADLIST.archive"),
    DOWNLOADLIST_STABLE("$DOWNLOADLIST.stable"),
    DOWNLOADLIST_TESTING("$DOWNLOADLIST.testing"),
    DOWNLOADLIST_DAILY("$DOWNLOADLIST.daily"),

    OPTIONS_TITLE("$OPTIONS.title"),
    OPTIONS_ROOTINSTALLFOLDER("$OPTIONS.rootinstallfolder"),
    OPTIONS_DOWNLOADFOLDER("$OPTIONS.downloadFolder"),
    OPTIONS_OPENFILE("$OPTIONS.choosefile"),
    OPTIONS_OPENFOLDER("$OPTIONS.choosefolder"),

    DOWNLOADER_TITLE("$DOWNLAODER.title"),
    DOWNLAODER_DOWNLOAD_FROM("$DOWNLAODER.downloadfrom"),
    DOWNLAODER_DOWNLOAD_TO("$DOWNLAODER.downloadto"),

    MAIN_TITLE("main.title"),
    CANCEL("cancel"),

    ERROR_FILEEXISRS("$ERROR.fileexists"),

    MENU_MANAGER("$MENU.manager"),
    MENU_ABOUT("$MENU.about"),
    MENU_LICENSE("$MENU.license"),

    CHOOSE_LIBREOFFICE("install.choose"),
    INSTALL_SUCCESS("install.success"),

    MANAGER_DELETE("$MANAGER.delete"),
    MANAGER_DELETE_SUCCESS("$MANAGER.deletesuccess"),
    MANAGER_DELETE_FAILURE("$MANAGER.deletefailure"),

    LICENSE_PRE("$LICENSE.pre"),
    LICENSE_LINK("$LICENSE.link");

    override fun toString(): String {
        return resourceBundleKey
    }
}

internal fun getSupportedLanguages(): Set<Locale> =
    newLocaleTreeSet().apply { add(Locale.forLanguageTag(EN_US)) }.apply {
        ResourceKey::class.java.classLoader.getResource("uistrings/sigui-desktop.properties").toURI().let {
            JarFileSystemProvider().let { fsp ->
                var fileSytemToCLose: FileSystem? = null
                try {
                    try {
                        //If in JAR
                        fsp.newFileSystem(it, emptyMap<String, Any?>()).let {
                            fileSytemToCLose = it
                            it.getPath("uistrings", "sigui-desktop.properties")
                        }
                    } catch (e: Exception) {
                        //Otherwise
                        Paths.get(it)
                    }.let {
                        it.parent.let { uiResourcePath ->
                            Files.list(uiResourcePath)
                                .map { it.fileName.toString() }
                                .filter { it.contains('_') }
                                .map { it.substring(it.indexOf('_') + 1, it.lastIndexOf('.')) }
                                .map { Locale.forLanguageTag(it) }
                                .forEach { add(it) }
                        }
                    }
                } finally {
                    fileSytemToCLose?.close()
                }
            }
        }
    }

internal const val EN_US = "en-US"