package com.example.orderlyphone.data.remote.adapter

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate

class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalDate {
        return LocalDate.parse(json.asString) // "2026-02-04"
    }

    override fun serialize(
        src: LocalDate,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}
