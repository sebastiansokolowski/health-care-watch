package com.selastiansokolowski.healthcarewatch.utils

import android.hardware.Sensor
import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase
import com.selastiansokolowski.healthcarewatch.model.healthCare.engine.AllSensorsEngine
import com.selastiansokolowski.healthcarewatch.model.healthCare.engine.EpilepsyEngine
import com.selastiansokolowski.healthcarewatch.model.healthCare.engine.FallEngine
import com.selastiansokolowski.healthcarewatch.model.healthCare.engine.HeartRateAnomalyEngine
import com.selastiansokolowski.shared.healthCare.HealthCareEventType

/**
 * Created by Sebastian Sokołowski on 06.08.19.
 */
class HealthCareEnginesUtils {
    companion object {
        fun getSupportedHealthCareEngines(sensors: List<Sensor>): Set<HealthCareEngineBase> {
            val sensorTypes = sensors.map { it.type }
            val supportedHealthCareEngines = mutableSetOf<HealthCareEngineBase>()

            HealthCareEventType.values().forEach { healthCareEventType ->
                getHealthCareEngine(healthCareEventType)?.let { healthCareEngine ->
                    if (sensorTypes.containsAll(healthCareEngine.requiredSensors())) {
                        supportedHealthCareEngines.add(healthCareEngine)
                    }
                }
            }

            return supportedHealthCareEngines
        }

        fun getHealthCareEngines(healthCareEventTypeNames: List<String>): Set<HealthCareEngineBase> {
            val healthCareEngines = mutableSetOf<HealthCareEngineBase>()

            healthCareEventTypeNames.forEach { healthCareEventTypeName ->
                try {
                    HealthCareEventType.valueOf(healthCareEventTypeName).let { healthCareEventType ->
                        getHealthCareEngine(healthCareEventType)?.let { healthCareEngine ->
                            healthCareEngines.add(healthCareEngine)
                        }
                    }
                } catch (e: IllegalArgumentException) {
                }
            }

            return healthCareEngines
        }

        private fun getHealthCareEngine(healthCareEventType: HealthCareEventType): HealthCareEngineBase? {
            return when (healthCareEventType) {
                HealthCareEventType.EPILEPSY -> EpilepsyEngine()
                HealthCareEventType.FALL -> FallEngine()
                HealthCareEventType.HEARTH_RATE_ANOMALY -> HeartRateAnomalyEngine()
                HealthCareEventType.ALL_SENSORS -> AllSensorsEngine()
                else -> null
            }
        }
    }
}