package at.reisisoft;

import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

fun <T> Iterable<T>.stream(parallel: Boolean = false) = StreamSupport.stream(spliterator(), parallel)

fun <T> Array<T>.stream() = Arrays.stream(this)

fun <T : Comparable<T>> Stream<T>.toSortedSet(): SortedSet<T> =
    collect({ TreeSet<T>() }, { set, elem -> set.add(elem) }, { a, b -> a.addAll(b) })

fun <T> Stream<T>.toList(): List<T> = collect(Collectors.toList())

fun <T> Stream<T>.checkpoint(continueParallel: Boolean = isParallel) =
    collect(Collectors.toList()).let { if (continueParallel) it.parallelStream() else it.stream() }

infix fun Path.withChild(child: String): Path = resolve(child)