package com.sebastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.sebastiansokolowski.shared.SettingsSharedPreferences
import com.sebastiansokolowski.shared.dataModel.HealthCareEventType
import com.sebastiansokolowski.shared.dataModel.settings.EpilepsySettings
import com.sebastiansokolowski.shared.dataModel.settings.FallSettings
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings

/**
 * Created by Sebastian Sokołowski on 23.08.19.
 */
class SettingsModel(private val sharedPreferences: SharedPreferences) {
    fun getMeasurementSettings(): MeasurementSettings {
        val defaultMeasurementSettings = MeasurementSettings()

        val samplingMs = sharedPreferences.getInt(SettingsSharedPreferences.SAMPLING_US, defaultMeasurementSettings.samplingMs)
        //fall
        val fallThreshold = sharedPreferences.getInt(SettingsSharedPreferences.FALL_THRESHOLD, defaultMeasurementSettings.fallSettings.threshold)
        val fallStepDetector = sharedPreferences.getBoolean(SettingsSharedPreferences.FALL_STEP_DETECTOR, defaultMeasurementSettings.fallSettings.stepDetector)
        val fallTimeOfInactivityS = sharedPreferences.getInt(SettingsSharedPreferences.FALL_TIME_OF_INACTIVITY_S, defaultMeasurementSettings.fallSettings.timeOfInactivity)
        val fallActivityThreshold = sharedPreferences.getInt(SettingsSharedPreferences.FALL_ACTIVITY_THRESHOLD, defaultMeasurementSettings.fallSettings.activityThreshold)
        //epilepsy
        val epilepsyThreshold = sharedPreferences.getInt(SettingsSharedPreferences.EPILEPSY_THRESHOLD, defaultMeasurementSettings.epilepsySettings.threshold)
        val epilepsyTime = sharedPreferences.getInt(SettingsSharedPreferences.EPILEPSY_TIME, defaultMeasurementSettings.epilepsySettings.timeS)


        val healthCareEvents = sharedPreferences.getStringSet(SettingsSharedPreferences.HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()

        val fallSettings = FallSettings(fallThreshold, fallStepDetector, fallTimeOfInactivityS, fallActivityThreshold)
        val epilepsySettings = EpilepsySettings(epilepsyThreshold, epilepsyTime)

        return MeasurementSettings(samplingMs, healthCareEvents, fallSettings, epilepsySettings)
    }

    fun getSupportedHealthCareEventTypes(): List<HealthCareEventType> {
        val healthCareEventsName = sharedPreferences
                .getStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()

        return healthCareEventsName.mapNotNull {
            try {
                HealthCareEventType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    fun saveSupportedHealthCareEvents(healthCareEvents: Set<HealthCareEventType>) {
        sharedPreferences.edit()?.apply {
            val values = healthCareEvents.map { sensor -> sensor.name }.toSet()
            putStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, values)
            commit()
        }
    }

    @SuppressLint("ApplySharedPref")
    fun saveHealthCareEvents(healthCareEvents: List<HealthCareEventType>) {
        sharedPreferences.edit()?.apply {
            val values = healthCareEvents.map { sensor -> sensor.name }.toSet()
            putStringSet(SettingsSharedPreferences.HEALTH_CARE_EVENTS, values)
            commit()
        }
    }

    fun isFirstSetupCompleted(): Boolean {
        return sharedPreferences.getBoolean(SettingsSharedPreferences.FIRST_SETUP_COMPLETED, false)
    }

    @SuppressLint("ApplySharedPref")
    fun setFirstSetupCompleted() {
        sharedPreferences.edit()?.apply {
            putBoolean(SettingsSharedPreferences.FIRST_SETUP_COMPLETED, true)
            commit()
        }
    }
}