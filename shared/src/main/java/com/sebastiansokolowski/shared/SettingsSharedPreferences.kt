package com.sebastiansokolowski.shared

/**
 * Created by Sebastian Sokołowski on 07.07.19.
 */
class SettingsSharedPreferences {
    companion object {
        const val CLEAR_DATABASE = "clear_database"
        const val HISTORY_DATA_EXPIRE_DAYS = "history_data_expire_days"
        const val CONTACTS = "contacts"
        const val HEALTH_EVENTS = "health_events"
        const val SUPPORTED_HEALTH_EVENTS = "supported_health_events"
        const val FIRST_SETUP_COMPLETED = "first_setup_completed"
        const val ANDROID_NOTIFICATIONS = "android_notifications_enabled"
        const val SMS_NOTIFICATIONS = "sms_notifications_enabled"
        const val SMS_USER_LOCATION = "sms_user_location_enabled"
        const val SAMPLING_US = "sampling_us"
        const val BATTERY_SAVER = "battery_saver"
        const val TEST_MODE = "test_mode"
        const val HEART_RATE_ANOMALY_ACTIVITY_DETECTOR_TIMEOUT_MIN = "heart_rate_anomaly_activity_detector_timeout_min"
        const val HEART_RATE_ANOMALY_MIN_THRESHOLD = "heart_rate_anomaly_min_threshold"
        const val HEART_RATE_ANOMALY_MAX_THRESHOLD_DURING_INACTIVITY = "heart_rate_anomaly_max_threshold_during_inactivity"
        const val HEART_RATE_ANOMALY_MAX_THRESHOLD_DURING_ACTIVITY = "heart_rate_anomaly_max_threshold_during_activity"
        const val FALL_THRESHOLD = "fall_threshold"
        const val FALL_SAMPLING_TIME_S = "fall_sampling_time_s"
        const val FALL_INACTIVITY_DETECTOR = "fall_inactivity_detector"
        const val FALL_INACTIVITY_DETECTOR_TIMEOUT_S = "fall_inactivity_detector_timeout_s"
        const val FALL_INACTIVITY_DETECTOR_THRESHOLD = "fall_inactivity_detector_threshold"
        const val EPILEPSY_THRESHOLD = "epilepsy_motion_threshold"
        const val EPILEPSY_SAMPLING_TIME_S = "epilepsy_sampling_time_s"
        const val EPILEPSY_MOTIONS = "epilepsy_motions"
    }
}