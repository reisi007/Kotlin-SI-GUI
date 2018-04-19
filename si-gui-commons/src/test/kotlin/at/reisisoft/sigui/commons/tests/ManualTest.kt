package at.reisisoft.sigui.commons.tests;

import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadLocation
import at.reisisoft.sigui.commons.downloads.DownloadType.*
import at.reisisoft.sigui.commons.downloads.LibreOfficeDownloadFileType
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper
import java.util.*
import kotlin.test.Test

class ManualTest {

    @JvmName("prettyPrintCollection")
    private fun Map<DownloadLocation, Set<DownloadInformation>>.prettyPrint() =
        TreeSet(keys).forEach { key ->
            println("== $key ==")
            println()
            getValue(key).forEach(::println)
            println()
            println()
        }


    private fun Map<LibreOfficeDownloadFileType, String>.prettyPrint() =
        TreeSet(keys).forEach { key ->
            println("== $key ==")
            println()
            getValue(key).let { println(it) }
            println()
            println()
        }


    @Test
    fun manualTestWindows() =
        PossibleDownloadHelper.fetchPossibleFor(
            arrayOf(
                WINDOWSEXE,
                WINDOWS32,
                WINDOWS64
            )
        ).prettyPrint()

    @Test
    fun manualTestSdViewer() =
        PossibleDownloadHelper.fetchPossibleFor(arrayOf(ANDROID_REMOTE)).prettyPrint()

    @Test
    fun manualTestAndroidViewer() =
        PossibleDownloadHelper.fetchPossibleFor(arrayOf(ANDROID_LIBREOFFICE_ARM)).prettyPrint()

    @Test
    fun manualTestLinux() =
        PossibleDownloadHelper.fetchPossibleFor(arrayOf(LINUX_RPM_32, LINUX_DEB_64)).prettyPrint()

    @Test
    fun finalDownloadLinkOldWindows() =
        "https://downloadarchive.documentfoundation.org/libreoffice/old/3.3.1.2/win/x86/".let { urlAsString ->
            PossibleDownloadHelper.getFinalDownloadUrls(
                urlAsString,
                setOf(
                    LibreOfficeDownloadFileType.MAIN,
                    LibreOfficeDownloadFileType.HP,
                    LibreOfficeDownloadFileType.SDK
                ),
                "de"
            ).prettyPrint()
        }

    @Test
    fun finalDownloadLinkStableWindows64() =
        "http://download.documentfoundation.org/libreoffice/stable/6.0.3/win/x86_64/".let { urlAsString ->
            PossibleDownloadHelper.getFinalDownloadUrls(
                urlAsString,
                setOf(
                    LibreOfficeDownloadFileType.MAIN,
                    LibreOfficeDownloadFileType.HP,
                    LibreOfficeDownloadFileType.SDK
                ),
                "en"
            ).prettyPrint()
        }
}