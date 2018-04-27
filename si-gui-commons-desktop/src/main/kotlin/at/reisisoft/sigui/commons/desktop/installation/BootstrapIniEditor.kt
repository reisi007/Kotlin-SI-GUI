package at.reisisoft.sigui.commons.installation

import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.streams.asSequence

object BootstrapIniEditor {

    private val possibleBootstrapIniFiles = setOf("bootstraprc", "bootstrap.ini")
    private val modifyRegex by lazy { Regex("UserInstallation=.*") }
    private val correctUserInstallationFolder = "UserInstallation=\$ORIGIN/.."

    fun modifyInFolder(path: Path) {
        Files.list(path).filter { possibleBootstrapIniFiles.contains(it.fileName.toString()) }
            .asSequence().first()?.let { bootstrapFile ->
                modifyBootstrapIni(bootstrapFile)
            } ?: throw FileNotFoundException("The bootstrap ini file could not be found in $path")
    }

    private fun modifyBootstrapIni(bootstrapFilePath: Path) {
        Files.newBufferedReader(bootstrapFilePath, StandardCharsets.UTF_8).readText().let { wholeFile ->
            modifyRegex.find(wholeFile)?.value?.let {
                wholeFile.replace(it, correctUserInstallationFolder)
                    .apply {
                        Files.newBufferedWriter(
                            bootstrapFilePath,
                            StandardCharsets.UTF_8,
                            StandardOpenOption.TRUNCATE_EXISTING
                        )
                            .use { writer ->
                                writer.write(this)
                            }
                    }
            }
        }
    }
}