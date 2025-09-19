package github.kasuminova.mmce.common.serialize.json

import github.kasuminova.mmce.common.serialize.raw.RawData
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

open class JsonRawData(val element: JsonElement) : RawData {

    override fun get(): Any = when (element) {
        is JsonPrimitive -> element.content
        is JsonArray -> element.map { JsonRawData(it).get() }
        is JsonObject -> element.mapValues { JsonRawData(it.value).get() }
    }

    override fun toString(): String = element.toString()

}