package com.selastiansokolowski.shared

/**
 * Created by Sebastian Sokołowski on 07.07.19.
 */
class SettingsSharedPreferences {
    companion object {
        // mobile
        const val CONTACTS = "contacts"
        const val HEALTH_CARE_EVENTS = "health_care_events"
        const val SUPPORTED_HEALTH_CARE_EVENTS = "supported_health_care_events"
        const val FIRST_SETUP_COMPLETED = "first_setup_completed"
        // mobile and wear
        const val SAMPLING_US = "sampling_us"
        const val SAMPLING_US_DEFAULT = 5
        // wear
        const val SENSORS = "sensors"
    }
}