package com.sebastiansokolowski.healthguard.utils

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.healthguard.model.healthGuard.engine.*
import com.sebastiansokolowski.shared.dataModel.HealthEventType

/**
 * Created by Sebastian Sokołowski on 06.08.19.
 */
class HealthEnginesUtils {
    companion object {
        fun getSupportedHealthEngines(sensors: List<Sensor>): Set<HealthGuardEngineBase> {
            val sensorTypes = sensors.map { it.type }
            val supportedHealthEngines = mutableSetOf<HealthGuardEngineBase>()

            HealthEventType.values().forEach { healthEventType ->
                getHealthEngine(healthEventType)?.let { healthEngine ->
                    if (sensorTypes.containsAll(healthEngine.requiredSensors())) {
                        supportedHealthEngines.add(healthEngine)
                    }
                }
            }

            return supportedHealthEngines
        }

        fun getHealthEngines(healthEventTypeNames: Set<String>): Set<HealthGuardEngineBase> {
            val healthEngines = mutableSetOf<HealthGuardEngineBase>()

            healthEventTypeNames.forEach { healthEventTypeName ->
                try {
                    HealthEventType.valueOf(healthEventTypeName).let { healthEventType ->
                        getHealthEngine(healthEventType)?.let { healthEngine ->
                            healthEngines.add(healthEngine)
                        }
                    }
                } catch (e: IllegalArgumentException) {
                }
            }

            return healthEngines
        }

        private fun getHealthEngine(healthEventType: HealthEventType): HealthGuardEngineBase? {
            return when (healthEventType) {
                HealthEventType.EPILEPSY -> EpilepsyEngine()
                HealthEventType.FALL -> FallEngine()
                HealthEventType.FALL_TORDU -> FallEngineTordu()
                HealthEventType.HEARTH_RATE_ANOMALY -> HeartRateAnomalyEngine()
                HealthEventType.ALL_SENSORS -> AllSensorsEngine()
                else -> null
            }
        }
    }
}