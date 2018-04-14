package at.reisisoft.sigui.settings

import at.reisisoft.withChild
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

@Serializable
internal data class SiGuiSetting(val test: String = "")/*TODO*/

internal fun storeSettings(settings: SiGuiSetting): Unit =
    JSON.stringify(settings).let { json ->
        Files.newBufferedWriter(
            SETTINGS_PATH,
            DEFAULT_CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        ).use { it.write(json) }
    }

internal fun loadSettings(): SiGuiSetting =
    SETTINGS_PATH.let {
        if (!Files.exists(it))
            return SiGuiSetting()
        Files.newBufferedReader(SETTINGS_PATH, DEFAULT_CHARSET).lines().collect(Collectors.joining()).let { json ->
            JSON.parse(json)
        }

    }

internal val SETTINGS_PATH by lazy {
    Paths.get(SiGuiSetting::class.java.classLoader.getResource(".").toURI()) withChild "si-gui.settings.json"
}


//private val GSON:  by lazy { Gson() }

private val DEFAULT_CHARSET = StandardCharsets.UTF_8!!