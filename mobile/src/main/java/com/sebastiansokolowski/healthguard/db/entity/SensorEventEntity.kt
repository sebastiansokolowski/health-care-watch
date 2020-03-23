package com.sebastiansokolowski.healthguard.db.entity

import com.sebastiansokolowski.healthguard.db.converter.FloatArrayConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Uid
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

    override fun toString(): String {
        return "SensorEventData(id=$id, type=$type, accuracy=$accuracy, timestamp=$timestamp, values=${Arrays.toString(values)})"
    }

}