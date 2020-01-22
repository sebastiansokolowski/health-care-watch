package com.sebastiansokolowski.shared

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class DataClientPaths {
    companion object {
        const val NODE_CAPABILITY = "health_care_watch"

        //messages
        const val START_MEASUREMENT = "/start_measurement"
        const val STOP_MEASUREMENT = "/stop_measurement"
        const val GET_MEASUREMENT = "/get_measurement"
        const val REQUEST_START_MEASUREMENT = "/request_start_measurement"
        const val START_LIVE_DATA = "/start_live_data"
        const val STOP_LIVE_DATA = "/stop_live_data"
        const val GET_SUPPORTED_HEALTH_CARE_EVENTS = "/get_supported_health_care_engines"

        //data
        const val SUPPORTED_HEALTH_CARE_EVENTS_MAP_PATH = "/supported_health_care_events"
        const val SUPPORTED_HEALTH_CARE_EVENTS_MAP_JSON = "json"

        const val HEALTH_SENSOR_MAP_PATH = "/sensor"
        const val HEALTH_SENSOR_MAP_JSON = "json"

        const val HEALTH_CARE_MAP_PATH = "/health_care"
        const val HEALTH_CARE_MAP_JSON = "json"

        const val MEASUREMENT_START_DATA_PATH = "/measurement_start_data"
        const val MEASUREMENT_START_DATA_JSON = "json"
    }
}