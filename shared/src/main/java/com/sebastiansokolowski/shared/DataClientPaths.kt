package com.sebastiansokolowski.shared

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class DataClientPaths {
    companion object {
        const val NODE_CAPABILITY = "health_care_watch"

        const val START_MEASUREMENT = "/start_measurement"
        const val STOP_MEASUREMENT = "/stop_measurement"
        const val GET_MEASUREMENT = "/get_measurement"
        const val REQUEST_START_MEASUREMENT = "/request_start_measurement"
        const val START_LIVE_DATA = "/start_live_data"
        const val STOP_LIVE_DATA = "/stop_live_data"
        const val GET_SUPPORTED_HEALTH_CARE_EVENTS = "/get_supported_health_care_engines"

        const val SUPPORTED_HEALTH_CARE_EVENTS_MAP_PATH = "/supported_health_care_events"
        const val SUPPORTED_HEALTH_CARE_EVENTS_MAP_TYPES = "types"
        const val SUPPORTED_HEALTH_CARE_EVENTS_MAP_TIMESTAMP = "timestamp"

        const val DATA_MAP_PATH = "/sensor"
        const val DATA_MAP_SENSOR_EVENT_VALUES_KEY = "values"
        const val DATA_MAP_SENSOR_EVENT_SENSOR_TYPE = "sensor"
        const val DATA_MAP_SENSOR_EVENT_ACCURACY_KEY = "accuracy"
        const val DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY = "timestamp"

        const val HEALTH_CARE_MAP_PATH = "/health_care"
        const val HEALTH_CARE_TYPE = "type"
        const val HEALTH_CARE_EVENT_DATA = "event_data"
        const val HEALTH_CARE_TIMESTAMP = "timestamp"

        const val MEASUREMENT_START_DATA = "/measurement_start_data"
        const val MEASUREMENT_START_DATA_SAMPLING_US = SettingsSharedPreferences.SAMPLING_US
        const val MEASUREMENT_START_DATA_HEALTH_CARE_EVENTS = SettingsSharedPreferences.SENSORS
        const val MEASUREMENT_START_DATA_TIMESTAMP = "timestamp"
    }
}