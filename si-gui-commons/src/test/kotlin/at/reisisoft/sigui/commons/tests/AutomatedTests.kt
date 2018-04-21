package at.reisisoft.sigui.commons.tests

import at.reisisoft.NamingUtils
import kotlin.test.Test
import kotlin.test.assertEquals

class AutomatedTests {

    @Test
    fun nameExtractionTest() = mapOf(
        "LibO_3.3.0_Win_x86_install_all_lang.exe" to "3.3.0",
        "LibO-Dev_4.0.0.0.beta1_Linux_x86-64_install-deb_en-US.tar.gz" to "4.0.0.0.beta1",
        "LibO_3.3.1_Linux_x86_install-rpm_en-US.tar.gz" to "3.3.1",
        "LibreOffice_4.1.5.1_Win_x86.msi" to "4.1.5.1",
        "LibreOffice_6.0.0.1_Win_x86.msi" to "6.0.0.1",
        "LibreOffice_6.0.3_Linux_x86_rpm.tar.gz" to "6.0.3",
        "master~2018-04-16_00.53.19_LibreOfficeViewer-strippedUI-debug.apk" to "master~2018-04-16",
        "master~2018-04-21_01.30.42_LibreOfficeDev_6.1.0.0.alpha0_Linux_x86_deb.tar.gz" to "master~2018-04-21",
        "libreoffice-6-0~2018-04-19_23.23.40_LibreOfficeDev_6.0.5.0.0_Linux_x86-64_deb.tar.gz" to "libreoffice-6-0~2018-04-19"
    ).let { testData ->
        testData.forEach { input, expected ->
            NamingUtils.extractName(input).let { actual ->
                assertEquals(expected, actual, "For input $input!")
            }
        }
    }
}