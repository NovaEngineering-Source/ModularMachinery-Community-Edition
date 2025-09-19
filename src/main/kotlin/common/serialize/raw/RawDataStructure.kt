package github.kasuminova.mmce.common.serialize.raw

interface RawDataStructure: RawData {

    fun get(name: String): RawData?

    fun keySet(): Set<String>

    fun getString(name: String): String?

    fun getBoolean(name: String): Boolean?

    fun getInt(name: String): Int?

    fun getLong(name: String): Long?

    fun getFloat(name: String): Float?

    fun getDouble(name: String): Double?

    fun getStructure(name: String): RawDataStructure?

    fun getList(name: String): List<RawData>?

}