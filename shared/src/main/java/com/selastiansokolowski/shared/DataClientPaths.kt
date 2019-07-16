package com.selastiansokolowski.shared

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class DataClientPaths {
    companion object {
        const val NODE_CAPABILITY = "health_care_watch"

        const val START_MEASUREMENT = "/start_measurement"
        const val STOP_MEASUREMENT = "/stop_measurement"
        const val GET_MEASUREMENT = "/get_measurement"
        const val START_LIVE_DATA = "/start_live_data"
        const val STOP_LIVE_DATA = "/stop_live_data"

        const val SUPPORTED_MAP_PATH = "/sensor_supported"
        const val SUPPORTED_MAP_SENSOR_TYPE = "type"
        const val SUPPORTED_MAP_SENSOR_SUPPORTED = "supported"

        const val DATA_MAP_PATH = "/sensor"
        const val DATA_MAP_SENSOR_EVENT_VALUES_KEY = "values"
        const val DATA_MAP_SENSOR_EVENT_SENSOR_TYPE = "sensor"
        const val DATA_MAP_SENSOR_EVENT_ACCURACY_KEY = "accuracy"
        const val DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY = "timestamp"

        const val ACCURACY_MAP_PATH = "/accuracy"
        const val ACCURACY_MAP_SENSOR_TYPE = "type"
        const val ACCURACY_MAP_SENSOR_ACCURACY = "accuracy"

        const val SETTINGS_MAP_PATH = "/settings"
        const val SETTINGS_MAP_SAMPLING_US = SettingsSharedPreferences.SAMPLING_US
        const val SETTINGS_MAP_SENSORS = SettingsSharedPreferences.SENSORS
    }
}