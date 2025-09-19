package github.kasuminova.mmce.common.serialize

import kotlin.reflect.KProperty

interface DataValue<V> {

    val name: String

    val nullable: Boolean

    val value: V

    val deserializer: DataValueDeserializer<Any, V>

    fun nullable(): DataValue<V?>

    fun notnull(): DataValue<V & Any>

    fun def(provider: () -> V): DataValue<V>

    fun <R> map(transform: (value: V, name: String) -> R): DataValue<R>

    fun <R> map(transform: (value: V) -> R): DataValue<R> = map { v, _ -> transform(v) }

}

interface DataValueDeserializer<I, O> {

    fun deserialize(input: I, name: String): O

}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <V> DataValue<V>.getValue(thisRef: Any?, property: KProperty<*>): V = value