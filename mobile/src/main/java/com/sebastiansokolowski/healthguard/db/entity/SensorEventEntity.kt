package com.sebastiansokolowski.healthguard.db.entity

import com.sebastiansokolowski.healthguard.db.converter.FloatArrayConverter
import io.objectbox.annotation.*
import io.objectbox.relation.ToOne
import java.util.*

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

    @Convert(converter = FloatArrayConverter::class, dbType = String::class)
    lateinit var values: FloatArray

    lateinit var measurementEventEntity: ToOne<MeasurementEventEntity>

    override fun toString(): String {
        return "SensorEventData(id=$id, type=$type, accuracy=$accuracy, timestamp=$timestamp, values=${Arrays.toString(values)})"
    }

}