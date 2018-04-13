package at.reisisoft;

import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

fun <T> Iterable<T>.stream(parallel: Boolean = false) = StreamSupport.stream(spliterator(), parallel)

fun <T : Comparable<T>> Stream<T>.toSortedSet() =
    collect({ TreeSet<T>() }, { set, elem -> set.add(elem) }, { a, b -> a.addAll(b) })