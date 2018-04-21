package at.reisisoft.ui

import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat
import java.util.*

internal typealias LocalizedNameFunction = (String) -> Unit

internal fun ResourceBundle.doLocalized(key: String, localizedNameFunction: LocalizedNameFunction): Unit =
    getString(key).let(localizedNameFunction)

internal fun ResourceBundle.getReplacedString(key: String, vararg replaceWith: Any?): String = getString(key).let {
    MessageFormat.format(it, *replaceWith)
}

internal fun ResourceBundle.doLocalizedReplace(
    key: String,
    vararg replaceWith: Any?,
    localizedNameFunction: LocalizedNameFunction
): Unit = getReplacedString(key, *replaceWith)
    .let(localizedNameFunction)

internal fun Throwable.stackTraceAsString(): String = StringWriter().also {
    printStackTrace(PrintWriter(it, true))
}.toString()