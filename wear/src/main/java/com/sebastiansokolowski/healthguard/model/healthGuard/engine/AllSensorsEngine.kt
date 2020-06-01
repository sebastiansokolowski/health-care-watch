package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.shared.dataModel.HealthEventType

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class AllSensorsEngine : HealthGuardEngineBase() {

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_HEART_RATE,
                Sensor.TYPE_STEP_COUNTER,
                Sensor.TYPE_LINEAR_ACCELERATION)
    }

    override fun startEngine() {
    }

    override fun stopEngine() {
    }

    override fun getHealthEventType(): HealthEventType {
        return HealthEventType.ALL_SENSORS
    }

}