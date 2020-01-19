package com.sebastiansokolowski.shared

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
        const val FALL_THRESHOLD = "fall_threshold"
        const val FALL_STEP_DETECTOR = "fall_step_detector"

        const val SAMPLING_US_DEFAULT = 1000
        const val FALL_THRESHOLD_DEFAULT = 20
        const val FALL_STEP_DETECTOR_DEFAULT = true
        // wear
        const val SENSORS = "sensors"
    }
}