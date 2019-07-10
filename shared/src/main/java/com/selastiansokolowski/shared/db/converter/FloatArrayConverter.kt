package com.selastiansokolowski.shared.db.converter

import io.objectbox.converter.PropertyConverter

/**
 * Created by Sebastian Sokołowski on 15.03.19.
 */
class FloatArrayConverter : PropertyConverter<FloatArray, String> {
    companion object {
        private val DELIMITER = ","
    }

    override fun convertToDatabaseValue(entityProperty: FloatArray?): String? {
        if (entityProperty != null && entityProperty.isNotEmpty()) {
            val stringBuilder = StringBuilder()
            entityProperty.forEachIndexed { i, value ->
                if (i > 0) {
                    stringBuilder.append(DELIMITER)
                }
                stringBuilder.append(value)
            }
            return stringBuilder.toString()
        }
        return null
    }

    override fun convertToEntityProperty(databaseValue: String?): FloatArray? {
        if (databaseValue != null && databaseValue.isNotEmpty()) {
            val list = mutableListOf<Float>()

            databaseValue.split(DELIMITER).forEach {
                list.add(it.toFloat())
            }
            return list.toFloatArray()
        }
        return null
    }

}