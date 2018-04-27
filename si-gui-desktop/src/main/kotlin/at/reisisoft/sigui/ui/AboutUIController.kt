package at.reisisoft.sigui.ui

import at.reisisoft.sigui.ManifestUtils
import at.reisisoft.sigui.SiGuiManifestEntries
import at.reisisoft.sigui.getValue
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import java.net.URL
import java.util.*

class AboutUIController : Initializable {

    private fun StringBuilder.newline() = append(System.lineSeparator())

    @FXML
    private lateinit var versionInfo: TextArea
    @FXML
    private lateinit var rootPane: ScrollPane

    private lateinit var languageSupport: ResourceBundle

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        languageSupport = resources!!

        versionInfo.text = buildString {
            append("Version information")
            newline()
            ManifestUtils.loadManifest().let { manifest ->
                SiGuiManifestEntries.values().asSequence().map { it to manifest.getValue(it) }
                    .filter { it.second != null }.forEach { (key, value) ->
                        append("$key: $value")
                        newline()
                    }
            }

            newline()
            append("Running on:")
            newline()
            sequenceOf(
                "Java version" to sequenceOf("java.version", "java.vendor", "java.vm.version"),
                "Operating system" to sequenceOf("os.name", "os.arch", "os.version")
            ).forEach { (key, value) ->
                append("$key: ")
                append(value.map { System.getProperty(it) }.joinToString(" ") { it })
                newline()
            }
        }
    }
}