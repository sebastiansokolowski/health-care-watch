package com.selastiansokolowski.healthcarewatch.db.entity

import com.selastiansokolowski.healthcarewatch.db.converter.FloatArrayConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * Created by Sebastian Sokołowski on 17.01.19.
 */
@Entity
data class SensorEventData(@Id var id: Long = 0,
                           var type: Int? = null,
                           var accuracy: Int? = null,
                           var timestamp: Long? = null,
                           @Convert(converter = FloatArrayConverter::class, dbType = String::class) var values: FloatArray? = null){}