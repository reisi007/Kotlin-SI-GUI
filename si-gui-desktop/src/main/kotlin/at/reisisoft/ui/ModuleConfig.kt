package at.reisisoft.ui

import at.reisisoft.sigui.ui.ResourceKey
import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat
import java.util.*

internal typealias LocalizedNameFunction = (String) -> Unit
internal typealias ReturningLocalizedNameFunction<T> = (String) -> T

internal fun ResourceBundle.doLocalized(key: ResourceKey, localizedNameFunction: LocalizedNameFunction): Unit =
    getString(key).let(localizedNameFunction)

internal fun ResourceBundle.getString(key: ResourceKey) = getString(key.toString())

internal fun <T> ResourceBundle.doLocalizedReturn(
    key: ResourceKey,
    localizedNameFunction: ReturningLocalizedNameFunction<T>
): T =
    getString(key.toString()).let(localizedNameFunction)

internal fun ResourceBundle.getReplacedString(key: ResourceKey, vararg replaceWith: Any?): String =
    getString(key.toString()).let {
        MessageFormat.format(it, *replaceWith)
    }

internal fun ResourceBundle.doLocalizedReplace(
    key: ResourceKey,
    vararg replaceWith: Any?,
    localizedNameFunction: LocalizedNameFunction
): Unit = getReplacedString(key, *replaceWith)
    .let(localizedNameFunction)

internal fun Throwable.stackTraceAsString(): String = StringWriter().also {
    printStackTrace(PrintWriter(it, true))
}.toString()