package com.sebastiansokolowski.healthguard.db.converter

import com.sebastiansokolowski.shared.dataModel.HealthEventType
import io.objectbox.converter.PropertyConverter

/**
 * Created by Sebastian Sokołowski on 24.06.19.
 */
class HealthEventConverter : PropertyConverter<HealthEventType, String> {
    override fun convertToDatabaseValue(entityProperty: HealthEventType): String {
        return entityProperty.name
    }

    override fun convertToEntityProperty(databaseValue: String): HealthEventType {
        return HealthEventType.valueOf(databaseValue)
    }

}