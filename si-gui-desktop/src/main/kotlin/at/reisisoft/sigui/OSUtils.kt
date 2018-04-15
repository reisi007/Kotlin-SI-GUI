package at.reisisoft.sigui

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
        WINDOWS, LINUX, MAC
    }
}