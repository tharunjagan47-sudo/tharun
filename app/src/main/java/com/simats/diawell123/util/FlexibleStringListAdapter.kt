package com.simats.diawell123.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

/**
 * Bulletproof Gson TypeAdapter & JsonDeserializer for List<String>.
 * Handles:
 * - JSON Array: ["item1", "item2"] -> List<String>
 * - JSON String containing JSON Array: "[\"item1\", \"item2\"]" -> List<String>
 * - Comma-delimited String: "item1, item2" -> List<String>
 * - Single String: "item1" -> listOf("item1")
 * - Null / Empty / Object: emptyList()
 */
class FlexibleStringListAdapter : TypeAdapter<List<String>>(),
    JsonDeserializer<List<String>>,
    JsonSerializer<List<String>> {

    override fun write(out: JsonWriter, value: List<String>?) {
        if (value == null) {
            out.beginArray()
            out.endArray()
            return
        }
        out.beginArray()
        for (item in value) {
            out.value(item)
        }
        out.endArray()
    }

    override fun read(reader: JsonReader): List<String> {
        return when (reader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                val list = mutableListOf<String>()
                reader.beginArray()
                while (reader.hasNext()) {
                    when (reader.peek()) {
                        JsonToken.STRING, JsonToken.NUMBER -> {
                            val str = reader.nextString().trim()
                            if (str.isNotEmpty()) list.add(str)
                        }
                        JsonToken.NULL -> {
                            reader.nextNull()
                        }
                        else -> {
                            reader.skipValue()
                        }
                    }
                }
                reader.endArray()
                list
            }
            JsonToken.STRING -> {
                val str = reader.nextString()
                parseStringToList(str)
            }
            JsonToken.NULL -> {
                reader.nextNull()
                emptyList()
            }
            else -> {
                reader.skipValue()
                emptyList()
            }
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String> {
        if (json == null || json.isJsonNull) return emptyList()

        if (json.isJsonArray) {
            val list = mutableListOf<String>()
            for (element in json.asJsonArray) {
                if (!element.isJsonNull) {
                    val str = element.asString.trim()
                    if (str.isNotEmpty()) list.add(str)
                }
            }
            return list
        }

        if (json.isJsonPrimitive) {
            val str = json.asString
            return parseStringToList(str)
        }

        return emptyList()
    }

    override fun serialize(
        src: List<String>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val array = JsonArray()
        src?.forEach { array.add(it) }
        return array
    }

    companion object {
        fun parseStringToList(raw: String?): List<String> {
            if (raw == null) return emptyList()
            val str = raw.trim()
            if (str.isEmpty() || str == "[]" || str.equals("null", ignoreCase = true)) {
                return emptyList()
            }
            if (str.startsWith("[") && str.endsWith("]")) {
                return try {
                    val parsed = Gson().fromJson(str, Array<String>::class.java)
                    parsed?.toList()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                } catch (_: Exception) {
                    str.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.replace("\"", "").replace("'", "").trim() }
                        .filter { it.isNotEmpty() }
                }
            }
            return str.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
    }
}

