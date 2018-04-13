package at.reisisoft.sigui.commons.tests;

import at.reisisoft.sigui.commons.downloads.DownloadLocation.ARCHIVE
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper
import kotlin.test.Test

class ManualTest {

    @Test
    fun manualTest() {
        PossibleDownloadHelper.fetchPossibleFor(DownloadType.WINDOWS64, ARCHIVE).forEach(System.out::println)

    }
}