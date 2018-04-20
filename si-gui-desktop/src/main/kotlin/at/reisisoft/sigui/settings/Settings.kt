package at.reisisoft.sigui.settings

import at.reisisoft.sigui.OSUtils
import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadLocation
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.withChild
import com.google.gson.*
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

internal data class SiGuiSetting(
    val downloadSelection: Pair<DownloadLocation, DownloadInformation?>? = null,
    val rootParallelInstallationFolder: Path = Files.createTempDirectory("si-gui"),
    val installFileMain: Path? = null,
    val installFileHelp: Path? = null,
    val installFileSdk: Path? = null,
    val hpLanguage: Locale? = null,
    val uiLanguage: Locale = Locale.getDefault(),
    val downloadTypes: List<DownloadType> = OSUtils.CURRENT_OS.downloadTypesForOS(),
    val downloadedVersions: Map<DownloadLocation, Set<DownloadInformation>> = emptyMap(),
    val availableHpLanguages: Collection<Locale> = emptyList()
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

private val JSON by lazy(GsonBuilder().registerTypeHierarchyAdapter(Path::class.java,
    object : JsonDeserializer<Path>, JsonSerializer<Path> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Path =
            Paths.get(json.asString)


        override fun serialize(src: Path, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            JsonPrimitive(src.toString())
    }
)::create)

//private val GSON:  by lazy { Gson() }

private val DEFAULT_CHARSET = StandardCharsets.UTF_8!!