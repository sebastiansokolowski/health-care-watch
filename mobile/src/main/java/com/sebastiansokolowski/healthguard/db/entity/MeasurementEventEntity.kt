package com.sebastiansokolowski.healthguard.db.entity

import com.google.gson.annotations.Expose
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

/**
 * Created by Sebastian Sokołowski on 24.06.19.
 */
@Entity
class MeasurementEventEntity {

    @Id
    var id: Long = 0
    var startTimestamp: Long = 0
    var stopTimestamp: Long = 0

    @Expose
    var measurementSettings: String = ""

    lateinit var sensorEventEntities: ToMany<SensorEventEntity>
    lateinit var healthEventEntities: ToMany<HealthEventEntity>

    override fun toString(): String {
        return "MeasurementEventEntity(id=$id, startTimestamp=$startTimestamp, stopTimestamp=$stopTimestamp, measurementSettings='$measurementSettings')"
    }

}