package com.selastiansokolowski.healthcarewatch.model

import android.content.SharedPreferences
import com.selastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.selastiansokolowski.shared.SettingsSharedPreferences
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
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
}