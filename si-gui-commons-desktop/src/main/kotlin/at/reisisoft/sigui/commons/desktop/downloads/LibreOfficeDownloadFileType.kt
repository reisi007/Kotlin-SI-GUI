package at.reisisoft.sigui.commons.desktop.downloads

import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.sigui.commons.downloads.LibreOfficeDownloadFileType
import at.reisisoft.sigui.commons.downloads.LibreOfficeDownloadFileType.Companion.getPredicateFor
import java.nio.file.Path
import java.nio.file.Paths

object LibreOfficeDownloadFileTypeUtils {
    @JvmStatic
    fun fromFilename(fileName: String, downlaodType: DownloadType) =
        fromFilename(Paths.get(fileName), downlaodType)

    @JvmStatic
    fun fromFilename(fileName: Path, downlaodType: DownloadType): LibreOfficeDownloadFileType =
        fileName.fileName.toString().let { fileNameAsString ->
            when {
                getPredicateFor(
                    LibreOfficeDownloadFileType.HP,
                    downlaodType,
                    "."
                )(fileNameAsString) -> LibreOfficeDownloadFileType.HP // HP langage "." is just one char in REGEX!
                getPredicateFor(
                    LibreOfficeDownloadFileType.SDK,
                    downlaodType
                )(fileNameAsString) -> LibreOfficeDownloadFileType.SDK
                else -> LibreOfficeDownloadFileType.MAIN
            }
        }
}