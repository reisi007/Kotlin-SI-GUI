package at.reisisoft.sigui.commons.tests;

import at.reisisoft.sigui.commons.downloads.DownloadLocation.*
import at.reisisoft.sigui.commons.downloads.DownloadType.*
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper
import kotlin.test.Test

class ManualTest {

    @Test
    fun manualTestWindows() =
        PossibleDownloadHelper.fetchPossibleFor(
            arrayOf(ARCHIVE),
            arrayOf(WINDOWS32, WINDOWS64)
        ).forEach(System.out::println)

    @Test
    fun manualTestSdViewer() =
        PossibleDownloadHelper.fetchPossibleFor(arrayOf(ARCHIVE), arrayOf(ANDROID_REMOTE)).forEach(System.out::println)

    @Test
    fun manualTestAndroidViewer() =
        PossibleDownloadHelper.fetchPossibleFor(
            arrayOf(ARCHIVE),
            arrayOf(ANDROID_LIBREOFFICE_ARM)
        ).forEach(System.out::println)

    @Test
    fun dailyBuilds() =
        PossibleDownloadHelper.fetchPossibleFor(
            arrayOf(DAILY),
            arrayOf(
                WINDOWS64,
                WINDOWS32,
                LINUX_DEB_64,
                LINUX_RPM_32,
                MAC,
                ANDROID_LIBREOFFICE_X86,
                ANDROID_LIBREOFFICE_ARM
            )
        ).forEach(System.out::println)


    @Test
    fun stable() =
        PossibleDownloadHelper.fetchPossibleFor(arrayOf(STABLE), arrayOf(WINDOWS64, WINDOWS32, LINUX_DEB_64)).forEach(
            System.out::println
        )

}