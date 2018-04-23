package at.reisisoft.sigui.commons.installation

import at.reisisoft.NamingUtils
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.withChild
import java.nio.file.Path
import java.nio.file.Paths

object ParallelInstallation {

    fun performInstallationFor(
        filepaths: List<String>,
        installLocation: Path,
        downloadType: DownloadType,
        shortcutCreator: ShortcutCreator?,
        shortcutLocation: Path?
    ): Array<Path> = performInstallationFor(
        filepaths,
        installLocation,
        ParallelInstallationOS.fromDownloadType(downloadType),
        shortcutCreator,
        shortcutLocation
    )

    fun performInstallationFor(
        filepaths: List<String>,
        installLocation: Path,
        parallelinstalationType: ParallelInstallationOS,
        shortcutCreator: ShortcutCreator?,
        shortcutLocation: Path?
    ): Array<Path> {
        filepaths.forEach {
            it.let { Paths.get(it).let { it.fileName.toString() to it.parent } }.also { (fileName, containingFolder) ->
                when (parallelinstalationType) {
                    ParallelInstallationOS.WINDOWS -> "msiexec" to "/qr /norestart /a \"$fileName\" TARGETDIR=\"$installLocation\""
                    else -> throw IllegalStateException("Parallel installation for $parallelinstalationType is not supported!")
                }.let { (program, commandlineArgs) ->
                    ProcessBuilder()
                        .directory(containingFolder.toFile())
                        .command(program, commandlineArgs)
                        .start()
                }.waitFor()
            }
        }

        return when (parallelinstalationType) {
            ParallelInstallationOS.WINDOWS -> "soffice.exe"
            else -> throw IllegalStateException("Parallel installation for $parallelinstalationType is not supported!")
        }.let { sofficeFileName ->

            val addedFiles = mutableListOf(installLocation)

            shortcutCreator?.let {
                (shortcutLocation
                        ?: throw IllegalStateException("Shortcut location mus not be null if shortcut creator is non-null")).let { rootPath ->
                    addedFiles.add(
                        it(
                            installLocation withChild "program" withChild sofficeFileName,
                            rootPath,
                            NamingUtils.extractName(filepaths.first())
                        )
                    )
                }
            }
            addedFiles.toTypedArray()
        }
    }
}


typealias ShortcutCreator = (Path, Path, String) -> Path


enum class ParallelInstallationOS {
    WINDOWS, LINUX_DEB, LINUX_RPM;

    companion object {
        @JvmStatic
        fun fromDownloadType(from: DownloadType): ParallelInstallationOS = when (from) {
            DownloadType.WINDOWS32, DownloadType.WINDOWS64 -> WINDOWS
            DownloadType.LINUX_RPM_32, DownloadType.LINUX_RPM_64 -> LINUX_RPM
            DownloadType.LINUX_DEB_32, DownloadType.LINUX_DEB_64 -> LINUX_DEB
            else -> throw DownloadTypeNotSupported(from)
        }
    }
}

class DownloadTypeNotSupported(val downloadType: DownloadType) :
    Exception("Parallel installation of \"$downloadType\" is not supported!")