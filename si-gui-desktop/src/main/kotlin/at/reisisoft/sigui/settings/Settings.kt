package at.reisisoft.sigui.settings

import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.withChild
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

internal data class SiGuiSetting(
    val downloadedVersions: Map<DownloadType, Set<DownloadInformation>> = emptyMap(),
    val uiLanguage: String = Locale.getDefault().toLanguageTag()
) {
    internal fun persist() = storeSettings(this)
}

internal fun storeSettings(settings: SiGuiSetting): Unit = Files.newBufferedWriter(
    SETTINGS_PATH, DEFAULT_CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
).use {
    JSON.writeValue(it, settings)
}

internal fun loadSettings(): SiGuiSetting =
    SETTINGS_PATH.let {
        if (!Files.exists(it))
            return@let SiGuiSetting()
        else
            return@let Files.newBufferedReader(SETTINGS_PATH, DEFAULT_CHARSET).use {
                JSON.readValue(it)
            }
    }

internal val SETTINGS_PATH by lazy {
    Paths.get(SiGuiSetting::class.java.classLoader.getResource(".").toURI()) withChild "si-gui.settings.json"
}

private val JSON by lazy { jacksonObjectMapper() }

//private val GSON:  by lazy { Gson() }

private val DEFAULT_CHARSET = StandardCharsets.UTF_8!!