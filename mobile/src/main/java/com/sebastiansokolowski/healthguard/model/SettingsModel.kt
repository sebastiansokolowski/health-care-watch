package com.sebastiansokolowski.healthguard.model

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.sebastiansokolowski.shared.SettingsSharedPreferences
import com.sebastiansokolowski.shared.dataModel.HealthEventType
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
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.ANDROID_NOTIFICATIONS, true)
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.SMS_NOTIFICATIONS, false)
            putSettingsWhenDoesNotExist(this, SettingsSharedPreferences.SMS_USER_LOCATION, false)
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

    //get

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


        val healthEvents = getHealthEvents()

        val fallSettings = FallSettings(fallThreshold, fallStepDetector, fallTimeOfInactivityS, fallActivityThreshold)
        val epilepsySettings = EpilepsySettings(epilepsyThreshold, epilepsyTime, epilepsyPercentOfPositiveEvents)

        return MeasurementSettings(samplingMs, healthEvents, fallSettings, epilepsySettings)
    }

    private fun getHealthEvents(): Set<HealthEventType> {
        val healthEvents = mutableSetOf<HealthEventType>()

        val healthEventsStringSet = sharedPreferences.getStringSet(SettingsSharedPreferences.HEALTH_EVENTS, emptySet())
                ?: emptySet()
        healthEventsStringSet.forEach { healthEventTypeName ->
            try {
                HealthEventType.valueOf(healthEventTypeName).let { healthEventType ->
                    healthEvents.add(healthEventType)
                }
            } catch (e: IllegalArgumentException) {
            }
        }

        return healthEvents
    }

    fun getSupportedHealthEventTypes(): List<HealthEventType> {
        val healthEventsName = sharedPreferences
                .getStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_EVENTS, emptySet())
                ?: emptySet()

        return healthEventsName.mapNotNull {
            try {
                HealthEventType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun getPhoneNumbers(): MutableSet<String>? {
        return sharedPreferences.getStringSet(SettingsSharedPreferences.CONTACTS, mutableSetOf())
    }

    fun isFirstSetupCompleted(): Boolean {
        return sharedPreferences.getBoolean(SettingsSharedPreferences.FIRST_SETUP_COMPLETED, false)
    }

    fun isAndroidNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(SettingsSharedPreferences.ANDROID_NOTIFICATIONS, true)
    }

    fun isSmsNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(SettingsSharedPreferences.SMS_NOTIFICATIONS, false)
    }

    fun isSmsUserLocationEnabled(): Boolean {
        return sharedPreferences.getBoolean(SettingsSharedPreferences.SMS_USER_LOCATION, false)
    }

    //set

    @SuppressLint("ApplySharedPref")
    fun saveSetting(key: String, value: Boolean) {
        sharedPreferences.edit()?.apply {
            putBoolean(key, value)
            commit()
        }
    }

    @SuppressLint("ApplySharedPref")
    fun saveSupportedHealthEvents(healthEvents: Set<HealthEventType>) {
        sharedPreferences.edit()?.apply {
            val values = healthEvents.map { sensor -> sensor.name }.toSet()
            putStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_EVENTS, values)
            commit()
        }
    }

    @SuppressLint("ApplySharedPref")
    fun saveHealthEvents(healthEvents: List<HealthEventType>) {
        sharedPreferences.edit()?.apply {
            val values = healthEvents.map { sensor -> sensor.name }.toSet()
            putStringSet(SettingsSharedPreferences.HEALTH_EVENTS, values)
            commit()
        }
    }

    @SuppressLint("ApplySharedPref")
    fun setFirstSetupCompleted() {
        sharedPreferences.edit()?.apply {
            putBoolean(SettingsSharedPreferences.FIRST_SETUP_COMPLETED, true)
            commit()
        }
    }
}