package at.reisisoft.sigui.commons.tests;

import at.reisisoft.sigui.commons.downloads.DownloadLocation.*
import at.reisisoft.sigui.commons.downloads.DownloadType.*
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper
import kotlin.test.Test

class ManualTest {

    @Test
    fun manualTestWindows() =
        PossibleDownloadHelper.fetchPossibleFor(ARCHIVE, WINDOWS32, WINDOWS64).forEach(System.out::println)

    @Test
    fun manualTestSdViewer() =
        PossibleDownloadHelper.fetchPossibleFor(ARCHIVE, ANDROID_REMOTE).forEach(System.out::println)

    @Test
    fun manualTestAndroidViewer() =
        PossibleDownloadHelper.fetchPossibleFor(ARCHIVE, ANDROID_LIBREOFFICE_ARM).forEach(System.out::println)

    @Test
    fun dailyBuilds() =
        PossibleDownloadHelper.fetchPossibleFor(DAILY, WINDOWS64, WINDOWS32, LINUX_DEB_64).forEach(System.out::println)

    @Test
    fun fresh() =
        PossibleDownloadHelper.fetchPossibleFor(FRESH, WINDOWS64, WINDOWS32, LINUX_DEB_64).forEach(System.out::println)

    @Test
    fun stable() =
        PossibleDownloadHelper.fetchPossibleFor(STABLE, WINDOWS64, WINDOWS32, LINUX_DEB_64).forEach(System.out::println)

}