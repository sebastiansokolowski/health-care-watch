package com.sebastiansokolowski.healthcarewatch.db.entity

import com.sebastiansokolowski.healthcarewatch.db.converter.FloatArrayConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

/**
 * Created by Sebastian Sokołowski on 17.01.19.
 */
@Entity
class SensorEventData {
    @Id
    var id: Long = 0
    var type: Int = 0
    var accuracy: Int = 0
    var timestamp: Long = 0
    @Convert(converter = FloatArrayConverter::class, dbType = String::class)
    lateinit var values: FloatArray

    override fun toString(): String {
        return "SensorEventData(id=$id, type=$type, accuracy=$accuracy, timestamp=$timestamp, values=${Arrays.toString(values)})"
    }

}