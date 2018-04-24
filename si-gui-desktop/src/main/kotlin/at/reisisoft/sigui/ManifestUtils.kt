package at.reisisoft.sigui

import at.reisisoft.sigui.ui.MainUI
import java.util.jar.Manifest

internal object ManifestUtils {
    fun loadManifest(): Manifest? =
        MainUI::class.java.classLoader.getResources("META-INF/MANIFEST.MF").let {
            it.asSequence().map {
                try {
                    Manifest(it.openStream()).let {
                        if (it.getValue(SiGuiManifestEntries.SI_GUI_VERSION) == null)
                            null
                        else
                            it
                    }
                } catch (e: Exception) {
                    null
                }
            }.filterNotNull().firstOrNull()
        }
}


internal fun Manifest.getValue(key: SiGuiManifestEntries): String? =
    mainAttributes.getValue(key.toString())


internal enum class SiGuiManifestEntries(private val internalKey: String) {
    SI_GUI_VERSION("Kotlin-SI-GUI-version"),
    BUILT_BY("Built-By"),
    BUIILD_TIMESTAMP("Build-Timestamp"),
    BUILD_JDK("Build-Jdk"),
    BUILD_OS("Build-OS");

    override fun toString(): String {
        return internalKey
    }
}