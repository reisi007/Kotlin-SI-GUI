package at.reisisoft.sigui

import at.reisisoft.sigui.commons.downloads.DownloadType

object OSUtils {

    val CURRENT_OS by lazy {
        System.getProperty("os.name").let { osName ->
            when {
                osName.contains("mac", true) -> OS.MAC
                osName.contains("win", true) -> OS.WINDOWS
                else -> OS.LINUX
            }
        }
    }

    enum class OS {
        WINDOWS, LINUX, MAC;

        fun downloadTypesForOS(): List<DownloadType> = when {
            this == WINDOWS -> listOf(DownloadType.WINDOWSEXE, DownloadType.WINDOWS32, DownloadType.WINDOWS64)
            this == LINUX -> listOf(
                DownloadType.LINUX_DEB_32,
                DownloadType.LINUX_DEB_64,
                DownloadType.LINUX_RPM_32,
                DownloadType.LINUX_RPM_64
            )
            else -> listOf(DownloadType.MAC)
        }
    }
}