package at.reisisoft.sigui.commons.desktop

object NamingUtils {

    private val masterNameRegex by lazy { Regex(".+~[0-9]{4}(-[0-9]{2}){2}") }
    private val regularVersionRegex by lazy { Regex("(?<name>[0-9]{1,}\\..*?)_") }

    fun extractName(mainFileName: String): String {
        masterNameRegex.find(mainFileName)?.let {
            return it.value
        }
        regularVersionRegex.find(mainFileName)?.let {
            it.groups[1]?.let {
                return it.value
            }
        }

        return mainFileName
    }
}