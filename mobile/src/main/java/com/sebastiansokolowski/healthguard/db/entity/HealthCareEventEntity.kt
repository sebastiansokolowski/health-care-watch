package com.sebastiansokolowski.healthguard.db.entity

import com.google.gson.annotations.Expose
import com.sebastiansokolowski.healthguard.db.converter.HealthCareEventConverter
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

    @Expose
    var value: Float = 0f

    lateinit var sensorEventEntity: ToOne<SensorEventEntity>

    @Expose
    @Convert(converter = HealthCareEventConverter::class, dbType = String::class)
    lateinit var careEvent: HealthCareEventType

    @Expose
    lateinit var details: String

    @Expose
    lateinit var measurementSettings: String

}