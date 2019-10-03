package com.sebastiansokolowski.healthcarewatch.model

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.shared.SettingsSharedPreferences
import com.sebastiansokolowski.shared.healthCare.HealthCareEventType
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 23.08.19.
 */
class SettingsModel(private val sharedPreferences: SharedPreferences) {
    fun getMeasurementSettings(): MeasurementSettings {
        val refreshRate = sharedPreferences.getInt(SettingsSharedPreferences.SAMPLING_US, SettingsSharedPreferences.SAMPLING_US_DEFAULT)

        val sampleUs = TimeUnit.SECONDS.toMicros(refreshRate.toLong()).toInt()
        val healthCareEvents = sharedPreferences.getStringSet(SettingsSharedPreferences.HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()

        return MeasurementSettings(sampleUs, ArrayList(healthCareEvents))
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
    fun saveSupportedHealthCareEvents(healthCareEvents: List<HealthCareEventType>) {
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