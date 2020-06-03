package com.sebastiansokolowski.healthguard.db.entity

import com.google.gson.annotations.Expose
import io.objectbox.annotation.Backlink
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

    @Backlink(to = "measurementEventEntity")
    lateinit var sensorEventEntities: ToMany<SensorEventEntity>
    @Backlink(to = "measurementEventEntity")
    lateinit var healthEventEntities: ToMany<HealthEventEntity>

}