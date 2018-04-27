package at.reisisoft.sigui.hostspecific

import at.reisisoft.sigui.OSUtils
import at.reisisoft.sigui.commons.desktop.installation.withChild
import at.reisisoft.sigui.commons.installation.ShortcutCreator
import net.jimmc.jshortcut.JShellLink
import java.nio.file.Files
import java.nio.file.Path

internal val SHORTCUT_CREATOR by lazy<ShortcutCreator> {
    when (OSUtils.CURRENT_OS) {
        OSUtils.OS.WINDOWS -> { targetFile: Path, shortcutRootFolder: Path, name: String ->
            JShellLink(shortcutRootFolder.toString(), name).apply {
                path = targetFile.toString()
            }.save()
            shortcutRootFolder withChild "$name.lnk"
        }
        OSUtils.OS.LINUX -> { targetFile: Path, shortcutRootFolder: Path, name: String ->
            (shortcutRootFolder withChild "LibreOffice $name.desktop").apply {
                Files.newBufferedWriter(this).use {
                    it.write(
                        """
                            [Desktop Entry]
                            Version=1.0
                            Type=Application
                            Name=LibreOffice $name
                            Terminal=false
                            NoDisplay=false
                            StartupNotify=false
                            Icon=libreoffice-startcenter
                            Exec=${targetFile.fileName}
                            Path=${targetFile.parent}
                    """.trimIndent()
                    )
                }
            }
        }
        else -> throw IllegalStateException("Creating shortcuts is not supported for ${OSUtils.CURRENT_OS}")
    }
}