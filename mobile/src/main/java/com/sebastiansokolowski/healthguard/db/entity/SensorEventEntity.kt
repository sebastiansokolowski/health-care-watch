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

    constructor()
    constructor(id: Long, type: Int, accuracy: Int, timestamp: Long, value: Float, measurementEventEntityId: Long) {
        this.id = id
        this.type = type
        this.accuracy = accuracy
        this.timestamp = timestamp
        this.value = value
        this.measurementEventEntity.targetId = measurementEventEntityId
    }

    override fun toString(): String {
        return "SensorEventData(id=$id, type=$type, accuracy=$accuracy, timestamp=$timestamp, value=${value})"
    }

}