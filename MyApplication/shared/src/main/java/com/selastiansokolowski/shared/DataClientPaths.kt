package com.selastiansokolowski.shared

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class DataClientPaths {
    companion object {
        const val START_MEASUREMENT = "/start_measurement"
        const val STOP_MEASUREMENT = "/stop_measurement"

        const val SUPPORTED_MAP_PATH = "/sensor_supported"
        const val SUPPORTED_MAP_SENSOR_TYPE = "type"
        const val SUPPORTED_MAP_SENSOR_SUPPORTED = "supported"

        const val DATA_MAP_PATH = "/sensor"
        const val DATA_MAP_SENSOR_EVENT_VALUES_KEY = "values"
        const val DATA_MAP_SENSOR_EVENT_SENSOR_KEY = "sensor"
        const val DATA_MAP_SENSOR_EVENT_ACCURACY_KEY = "accuracy"
        const val DATA_MAP_SENSOR_EVENT_TIMESTAMP_KEY = "timestamp"

        const val ACCURACY_MAP_PATH = "/accuracy"
        const val ACCURACY_MAP_SENSOR_TYPE = "type"
        const val ACCURACY_MAP_SENSOR_ACCURACY  = "accuracy"
    }
}