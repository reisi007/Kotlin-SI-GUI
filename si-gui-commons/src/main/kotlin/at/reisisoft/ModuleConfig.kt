package at.reisisoft;

import java.nio.file.Path
import java.util.function.Predicate

infix fun Path.withChild(child: String): Path = resolve(child)

infix fun <T> Predicate<T>.and(other: Predicate<T>) = this.and(other)

fun <T : Comparable<T>> comparing(thiz: T, other: T, ifUndecideable: () -> Int): Int =
    thiz.compareTo(other).let { result ->
        if (result != 0)
            return@let result
        return@let ifUndecideable()
    }

//https://stackoverflow.com/a/23088000/1870799
fun Double.format(digits: Int): String = java.lang.String.format("%.${digits}f", this)

fun <T> T?.orElse(value: T): T = this ?: value
fun <T> T?.orElse(valueSupplier: () -> T): T = this ?: valueSupplier()