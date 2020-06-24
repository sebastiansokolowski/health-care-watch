package com.sebastiansokolowski.healthguard.db.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

/**
 * Created by Sebastian Sokołowski on 17.01.19.
 */
@Entity
class SensorEventEntity {
    @Id
    var id: Long = 0
    var type: Int = 0
    var accuracy: Int = 0
    var timestamp: Long = 0
    var value: Float = 0f

    lateinit var measurementEventEntity: ToOne<MeasurementEventEntity>

    override fun toString(): String {
        return "SensorEventData(id=$id, type=$type, accuracy=$accuracy, timestamp=$timestamp, value=${value})"
    }

}