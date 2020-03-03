package com.sebastiansokolowski.healthcarewatch.db.entity

import com.sebastiansokolowski.healthcarewatch.db.converter.HealthCareEventConverter
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

/**
 * Created by Sebastian Sokołowski on 24.06.19.
 */
@Entity
class HealthCareEventEntity {
    @Id
    var id: Long = 0

    var value: Float = 0f

    lateinit var sensorEventEntity: ToOne<SensorEventEntity>
    @Convert(converter = HealthCareEventConverter::class, dbType = String::class)
    lateinit var careEvent: HealthCareEventType

    lateinit var details: String

    lateinit var measurementSettings: String

}