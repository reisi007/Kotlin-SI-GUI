package at.reisisoft.sigui.commons.desktop.installation

import java.nio.file.Path
import java.util.function.Predicate

infix fun Path.withChild(child: String): Path = resolve(child)

infix fun <T> Predicate<T>.and(other: Predicate<T>) = this.and(other)