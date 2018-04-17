package at.reisisoft.sigui.settings

import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadLocation
import at.reisisoft.withChild
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

internal data class SiGuiSetting(
    val downloadedVersions: Map<DownloadLocation, Set<DownloadInformation>> = emptyMap(),
    val downloadSelection: Pair<DownloadLocation, DownloadInformation?>? = null,
    val uiLanguage: String = Locale.getDefault().toLanguageTag()
) {
    internal fun persist() = storeSettings(this)
}

internal fun storeSettings(settings: SiGuiSetting): Unit = Files.newBufferedWriter(
    SETTINGS_PATH, DEFAULT_CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
).use {
    println("Storing settings to $SETTINGS_PATH")
    JSON.toJson(settings, it)
}

internal fun loadSettings(): SiGuiSetting {
    println("Loading setings from: $SETTINGS_PATH")
    if (!Files.exists(SETTINGS_PATH))
        return SiGuiSetting()
    else
        return Files.newBufferedReader(SETTINGS_PATH, DEFAULT_CHARSET).use {
            JSON.fromJson(it, SiGuiSetting::class.java)
        }
}

internal val SETTINGS_PATH by lazy {
    Paths.get(SiGuiSetting::class.java.classLoader.getResource(".").toURI()) withChild "si-gui.settings.json"
}

private val JSON by lazy { Gson() }

//private val GSON:  by lazy { Gson() }

private val DEFAULT_CHARSET = StandardCharsets.UTF_8!!