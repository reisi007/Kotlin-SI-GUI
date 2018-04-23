package at.reisisoft.sigui.hostspecific

import at.reisisoft.sigui.OSUtils
import at.reisisoft.sigui.commons.installation.ShortcutCreator
import at.reisisoft.withChild
import net.jimmc.jshortcut.JShellLink
import java.nio.file.Path

internal val SHORTCUT_CREATOR by lazy<ShortcutCreator> {
    when (OSUtils.CURRENT_OS) {
        OSUtils.OS.WINDOWS -> { targetFile: Path, shortcutRootFolder: Path, name: String ->
            JShellLink(shortcutRootFolder.toString(), name).apply {
                path = targetFile.toString()
            }.save()
            shortcutRootFolder withChild "$name.lnk"
        }
        else -> throw IllegalStateException("Creating shortcuts is not supported for ${OSUtils.CURRENT_OS}")
    }
}