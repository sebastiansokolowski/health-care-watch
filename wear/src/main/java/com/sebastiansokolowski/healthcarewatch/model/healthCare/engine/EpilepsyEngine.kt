package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType

/**
 * Created by Sebastian Sokołowski on 07.06.19.
 */
class EpilepsyEngine : HealthCareEngineBase() {

    override fun requiredSensors(): Set<Int> {
        return setOf(Sensor.TYPE_HEART_RATE)
    }

    override fun startEngine() {
    }

    override fun stopEngine() {
    }

    override fun getHealthCareEventType(): HealthCareEventType {
        return HealthCareEventType.EPILEPSY
    }

}