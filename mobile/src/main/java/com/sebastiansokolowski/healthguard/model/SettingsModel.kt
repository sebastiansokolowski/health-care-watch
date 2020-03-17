package com.sebastiansokolowski.healthguard.model

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

    init {
        setDefaultMeasurementSettings()
    }

    private fun setDefaultMeasurementSettings() {
        val defaultMeasurementSettings = MeasurementSettings()

        sharedPreferences.edit()?.apply {
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.SAMPLING_US, defaultMeasurementSettings.samplingMs)
            //fall
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.FALL_THRESHOLD, defaultMeasurementSettings.fallSettings.threshold)
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.FALL_STEP_DETECTOR, defaultMeasurementSettings.fallSettings.stepDetector)
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.FALL_TIME_OF_INACTIVITY_S, defaultMeasurementSettings.fallSettings.timeOfInactivity)
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.FALL_ACTIVITY_THRESHOLD, defaultMeasurementSettings.fallSettings.activityThreshold)
            //epilepsy
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.EPILEPSY_THRESHOLD, defaultMeasurementSettings.epilepsySettings.threshold)
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.EPILEPSY_TIME, defaultMeasurementSettings.epilepsySettings.timeS)
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.EPILEPSY_PERCENT_OF_POSITIVE_EVENTS, defaultMeasurementSettings.epilepsySettings.percentOfPositiveSignals)

            apply()
        }
    }

    private fun putSettingsWhenDoesNotExist(editor: SharedPreferences.Editor, key: String, value: Boolean) {
        if (!sharedPreferences.contains(key)) {
            editor.putBoolean(key, value)
        }
    }

    private fun putSettingsWhenDoesNotExist(editor: SharedPreferences.Editor, key: String, value: Int) {
        if (!sharedPreferences.contains(key)) {
            editor.putInt(key, value)
        }
    }

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
        val epilepsyPercentOfPositiveEvents = sharedPreferences.getInt(SettingsSharedPreferences.EPILEPSY_PERCENT_OF_POSITIVE_EVENTS, defaultMeasurementSettings.epilepsySettings.percentOfPositiveSignals)


        val healthCareEvents = sharedPreferences.getStringSet(SettingsSharedPreferences.HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()

        val fallSettings = FallSettings(fallThreshold, fallStepDetector, fallTimeOfInactivityS, fallActivityThreshold)
        val epilepsySettings = EpilepsySettings(epilepsyThreshold, epilepsyTime, epilepsyPercentOfPositiveEvents)

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