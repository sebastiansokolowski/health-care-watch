package com.sebastiansokolowski.healthguard.utils

import com.google.gson.*
import com.sebastiansokolowski.shared.util.Utils
import java.lang.reflect.Type


class SensorEventValuesSerializer : JsonSerializer<FloatArray> {
    override fun serialize(src: FloatArray?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonArray {
        return if (src == null) {
            JsonArray(0)
        } else {
            val jsonArray = JsonArray(src.size)

            src.forEach {
                jsonArray.add(Utils.roundValue(it))
            }
            jsonArray
        }
    }
}