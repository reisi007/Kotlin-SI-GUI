package at.reisisoft.ui

import java.util.*

fun ResourceBundle.doLocalized(key: String, localizedNameFunction: (String) -> Unit): Unit =
    getString(key).let(localizedNameFunction)