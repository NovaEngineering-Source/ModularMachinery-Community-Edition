package github.kasuminova.mmce.common.serialize.json

import github.kasuminova.mmce.common.serialize.raw.RawData
import github.kasuminova.mmce.common.serialize.raw.RawDataStructure
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long

class JsonRawDataStructure(val element: JsonObject) : RawDataStructure {

    override fun get(): Any = element.toString()

    override fun get(name: String): RawData? = element[name]?.let { JsonRawData(it) }

    override fun keySet(): Set<String> = element.keys

    override fun getString(name: String): String? = element[name]?.toString()

    override fun getBoolean(name: String): Boolean? = element[name]?.let { if (it is JsonPrimitive) it.boolean else null }

    override fun getInt(name: String): Int? = element[name]?.let { if (it is JsonPrimitive) it.int else null }

    override fun getLong(name: String): Long? = element[name]?.let { if (it is JsonPrimitive) it.long else null }

    override fun getFloat(name: String): Float? = element[name]?.let { if (it is JsonPrimitive) it.float else null }

    override fun getDouble(name: String): Double? = element[name]?.let { if (it is JsonPrimitive) it.double else null }

    override fun getStructure(name: String): RawDataStructure? = element[name]?.let { if (it is JsonObject) JsonRawDataStructure(it) else null }

    override fun getList(name: String): List<RawData>? = element[name]?.let { if (it is JsonArray) it.map { e -> JsonRawData(e) } else null }

}