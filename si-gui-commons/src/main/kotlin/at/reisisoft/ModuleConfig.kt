package at.reisisoft;

import java.nio.file.Path
import java.util.*
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.collections.HashMap

fun <T> Iterable<T>.stream(parallel: Boolean = false) = StreamSupport.stream(spliterator(), parallel)

fun <T> Array<T>.stream() = Arrays.stream(this)

fun <T : Comparable<T>> Stream<T>.toSortedSet(): SortedSet<T> =
    collect({ TreeSet<T>() }, { set, elem -> set.add(elem) }, { a, b -> a.addAll(b) })

fun <T> Stream<T>.toList(): List<T> = collect(Collectors.toList())

fun <T> Stream<T>.checkpoint(continueParallel: Boolean = isParallel) =
    collect(Collectors.toList()).let { if (continueParallel) it.parallelStream() else it.stream() }


fun <K, E : Comparable<E>> Stream<Pair<K, SortedSet<E>>>.toMap(): Map<K, SortedSet<E>> =
    collect(object : Collector<Pair<K, SortedSet<E>>, MutableMap<K, TreeSet<E>>, Map<K, SortedSet<E>>> {
        override fun characteristics(): Set<Collector.Characteristics> = setOf(Collector.Characteristics.UNORDERED)

        override fun supplier(): Supplier<MutableMap<K, TreeSet<E>>> = Supplier<MutableMap<K, TreeSet<E>>> { HashMap() }

        override fun accumulator(): BiConsumer<MutableMap<K, TreeSet<E>>, Pair<K, SortedSet<E>>> {
            return BiConsumer { map, (key, value) ->
                map.computeIfAbsent(key, { TreeSet() }).addAll(value)
            }
        }

        override fun combiner(): BinaryOperator<MutableMap<K, TreeSet<E>>> {
            return BinaryOperator { thiz, other ->
                other.keys.forEach { otherKey ->
                    val otherValues = other.getValue(otherKey)
                    thiz.computeIfAbsent(otherKey, { TreeSet() }).addAll(otherValues)
                }
                return@BinaryOperator thiz
            }
        }


        override fun finisher(): Function<MutableMap<K, TreeSet<E>>, Map<K, SortedSet<E>>> =
            Function { it }

    })

infix fun Path.withChild(child: String): Path = resolve(child)