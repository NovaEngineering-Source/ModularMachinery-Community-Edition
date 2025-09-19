package github.kasuminova.mmce.common.serialize

import github.kasuminova.mmce.common.serialize.json.JsonRawDataStructure
import github.kasuminova.mmce.common.serialize.raw.RawDataStructure
import kotlinx.serialization.json.JsonObject

@Suppress("UNCHECKED_CAST")
abstract class DataStructure {

    val columns: MutableList<DataValue<*>> = mutableListOf()

    fun loadFrom(json: JsonObject) = loadFrom(JsonRawDataStructure(json))

    fun loadFrom(raw: RawDataStructure) {
        for (column in this.columns) {
            try {
                (column as DataValueImpl).solve(raw)
            } catch (e: Throwable) {
                throw IllegalArgumentException("Failed to parse column '${column.name}'", e)
            }
        }
    }

    protected fun raw(name: String): DataValue<Any?> = DataValueImpl<Any?>(
        name = name,
        nullable = true,
        deserializer = AnyToAnyDeserializer as DataValueDeserializer<Any, Any?>,
        defProvider = { null }
    ).also { columns += it }

    protected fun byte(name: String): DataValue<Byte> = raw(name)
        .notnull()
        .map { value -> (value as? Number)?.toByte() ?: value.toString().toByte() }
        .def { 0 }

    protected fun short(name: String): DataValue<Short> = raw(name)
        .notnull()
        .map { value -> (value as? Number)?.toShort() ?: value.toString().toShort() }
        .def { 0 }

    protected fun integer(name: String): DataValue<Int> = raw(name)
        .notnull()
        .map { value -> (value as? Number)?.toInt() ?: value.toString().toInt() }
        .def { 0 }

    protected fun long(name: String): DataValue<Long> = raw(name)
        .notnull()
        .map { value -> (value as? Number)?.toLong() ?: value.toString().toLong() }
        .def { 0L }

    protected fun float(name: String): DataValue<Float> = raw(name)
        .notnull()
        .map { value -> (value as? Number)?.toFloat() ?: value.toString().toFloat() }
        .def { 0f }

    protected fun double(name: String): DataValue<Double> = raw(name)
        .notnull()
        .map { value -> (value as? Number)?.toDouble() ?: value.toString().toDouble() }
        .def { 0.0 }

    protected fun boolean(name: String): DataValue<Boolean> = raw(name)
        .notnull()
        .map { value -> (value as? Boolean) ?: value.toString().toBoolean() }
        .def { false }

    protected fun string(name: String): DataValue<String> = raw(name)
        .notnull()
        .map { value -> value.toString() }
        .def { "" }

    protected fun <T> list(name: String, mapping: (Any) -> T) = raw(name)
        .notnull()
        .map {
            if (it is List<*>) {
                it.map { e -> mapping(e!!) }
            } else {
                emptyList()
            }
        }

    protected fun subData(name: String): DataValue<RawDataStructure?> = raw(name)
        .nullable()
        .map { value -> value as? RawDataStructure }

    protected fun subStructure(name: String, structure: DataStructure) = raw(name)
        .nullable()
        .map { value -> value as? RawDataStructure }
        .map { raw ->
            structure.also { structure ->
                if (raw != null) {
                    structure.loadFrom(raw)
                }
            }
        }

    protected fun multipleStructure(name: String, provider: () -> DataStructure) = raw(name)
        .nullable()
        .map { value -> value as? RawDataStructure }
        .map { raw ->
            raw?.getStructure(name)?.let { structure ->
                structure.keySet().associateWith { key -> provider().also { data -> data.loadFrom(structure.getStructure(key)!!) } }
            }
        }

    protected fun <E> enum(name: String, values: Array<E>): DataValue<E> where E : Enum<E> = raw(name)
        .notnull()
        .map { value ->
            val str = value.toString()
            values.firstOrNull { it.name == str }
                ?: throw IllegalArgumentException("Value '$str' is not valid for enum '${values.firstOrNull()?.javaClass?.name}'")
        }

}