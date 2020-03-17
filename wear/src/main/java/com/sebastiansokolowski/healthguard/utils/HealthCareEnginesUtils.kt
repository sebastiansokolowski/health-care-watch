package com.sebastiansokolowski.healthguard.utils

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthCare.HealthCareEngineBase
import com.sebastiansokolowski.healthguard.model.healthCare.engine.*
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType

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

        fun getHealthCareEngines(healthCareEventTypeNames: Set<String>): Set<HealthCareEngineBase> {
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
                HealthCareEventType.FALL_TORDU -> FallEngineTordu()
                HealthCareEventType.HEARTH_RATE_ANOMALY -> HeartRateAnomalyEngine()
                HealthCareEventType.ALL_SENSORS -> AllSensorsEngine()
                else -> null
            }
        }
    }
}