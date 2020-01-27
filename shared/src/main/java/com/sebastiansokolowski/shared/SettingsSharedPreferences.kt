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
        const val FALL_TIME_OF_INACTIVITY_S = "fall_time_of_inactivity_s"
        const val FALL_ACTIVITY_THRESHOLD = "fall_activity_threshold"
        const val EPILEPSY_THRESHOLD = "epilepsy_threshold"
        const val EPILEPSY_TIME = "epilepsy_time"


        // wear
        const val SENSORS = "sensors"
    }
}