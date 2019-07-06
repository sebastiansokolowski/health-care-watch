package com.selastiansokolowski.healthcarewatch.db.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * Created by Sebastian Sokołowski on 03.02.19.
 */
@Entity
class SensorEventSupportedInfo {
    @Id
    var id: Long = 0
    var type: Int = 0
    var supported: Boolean = false
}