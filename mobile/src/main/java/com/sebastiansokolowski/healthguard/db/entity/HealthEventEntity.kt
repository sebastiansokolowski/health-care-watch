package com.sebastiansokolowski.healthguard.db.entity

import com.google.gson.annotations.Expose
import com.sebastiansokolowski.healthguard.db.converter.HealthEventConverter
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

/**
 * Created by Sebastian Sokołowski on 24.06.19.
 */
@Entity
class HealthEventEntity {

    @Id
    var id: Long = 0

    @Expose
    var value: Float = 0f

    lateinit var sensorEventEntity: ToOne<SensorEventEntity>

    @Expose
    @Convert(converter = HealthEventConverter::class, dbType = String::class)
    lateinit var event: HealthEventType

    @Expose
    lateinit var details: String

    lateinit var measurementEventEntity: ToOne<MeasurementEventEntity>

    constructor()
    constructor(id: Long, value: Float, sensorEventEntityId: Long, event: HealthEventType, details: String, measurementEventEntityId: Long) {
        this.id = id
        this.value = value
        this.sensorEventEntity.targetId = sensorEventEntityId
        this.event = event
        this.details = details
        this.measurementEventEntity.targetId = measurementEventEntityId
    }

    override fun toString(): String {
        return "HealthEventEntity(id=$id, value=$value, sensorEventEntity=$sensorEventEntity, event=$event, details='$details', measurementEventEntity=$measurementEventEntity)"
    }

}