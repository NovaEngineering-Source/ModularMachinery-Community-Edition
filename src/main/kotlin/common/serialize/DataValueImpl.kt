package github.kasuminova.mmce.common.serialize

@Suppress("UNCHECKED_CAST")
class DataValueImpl<V>(
    override val name: String,
    override var nullable: Boolean = false,
    override var deserializer: DataValueDeserializer<Any, V>,
    var defProvider: () -> V
) : DataValue<V> {

    private var solved: Boolean = false

    private var _cached: V? = null

    override val value: V
        get() {
            if (!solved) {
                _cached = defProvider()
                require(!nullable && _cached == null) { "Value '$name' is not nullable, but got null" }
                solved = true
            }
            return _cached as V
        }

    override fun nullable(): DataValue<V?> {
        nullable = true
        return this as DataValue<V?>
    }

    override fun notnull(): DataValue<V & Any> {
        nullable = false
        return this as DataValue<V & Any>
    }

    override fun def(provider: () -> V): DataValue<V> {
        defProvider = provider
        return this
    }

    override fun <R> map(transform: (value: V, name: String) -> R): DataValue<R> {
        val prevDeserializer = deserializer
        val prevDefProvider = defProvider
        val ret = this as DataValueImpl<R>
        ret.deserializer = MappingDeserializer(prevDeserializer, transform)
        ret.defProvider = { transform(prevDefProvider(), name) }
        if (solved) {
            ret._cached = transform(_cached as V, name)
        }
        return ret
    }

    fun solve(input: Any) {
        _cached = deserializer.deserialize(input, name)
        require(!nullable && _cached == null) { "Value '$name' is not nullable, but got null" }
        solved = true
    }

}

internal class MappingDeserializer<I, O, R>(val prev: DataValueDeserializer<I, O>, val transform: (O, String) -> R) : DataValueDeserializer<I, R> {

    override fun deserialize(input: I, name: String): R = transform(prev.deserialize(input, name), name)

}

internal object AnyToAnyDeserializer : DataValueDeserializer<Any, Any> {

    override fun deserialize(input: Any, name: String): Any = input

}

internal object NothingDeserializer : DataValueDeserializer<Any, Nothing?> {

    override fun deserialize(input: Any, name: String): Nothing? = null

}