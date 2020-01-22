package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class AllSensorsEngine : HealthCareEngineBase() {

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_HEART_RATE,
                Sensor.TYPE_STEP_COUNTER,
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_LINEAR_ACCELERATION)
    }

    override fun startEngine() {
    }

    override fun stopEngine() {
    }

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.ALL_SENSORS
    }

}