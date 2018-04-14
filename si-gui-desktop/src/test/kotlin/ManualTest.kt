import at.reisisoft.sigui.settings.SETTINGS_PATH
import at.reisisoft.sigui.settings.loadSettings
import at.reisisoft.sigui.settings.storeSettings
import java.nio.file.Files
import kotlin.test.Test

class ManualTest {

    @Test
    fun defaultSettings() {
        Files.deleteIfExists(
            SETTINGS_PATH.also { println("Settings file is stored at: $SETTINGS_PATH") }
        ).also { deleted -> if (deleted) println("Deleted existing settings file!") }
        for (i in 1..2)
            loadSettings().let {
                println("Settings loaded!")
                storeSettings(it)
                println("Settings stored!")
            }
    }
}