package com.sebastiansokolowski.shared

/**
 * Created by Sebastian Sokołowski on 16.07.18.
 */
class DataClientPaths {
    companion object {
        const val NODE_CAPABILITY = "health_guard"

        //messages
        const val START_MEASUREMENT_PATH = "/start_measurement"
        const val STOP_MEASUREMENT_PATH = "/stop_measurement"
        const val GET_MEASUREMENT_PATH = "/get_measurement"
        const val REQUEST_START_MEASUREMENT_PATH = "/request_start_measurement"
        const val START_LIVE_DATA_PATH = "/start_live_data"
        const val STOP_LIVE_DATA_PATH = "/stop_live_data"
        const val GET_SUPPORTED_HEALTH_EVENTS_PATH = "/get_supported_health_engines"

        //data
        const val SUPPORTED_HEALTH_EVENTS_MAP_PATH = "/supported_health_events"
        const val SUPPORTED_HEALTH_EVENTS_MAP_JSON = "json"

        const val SENSOR_EVENTS_MAP_PATH = "/sensor_events"
        const val SENSOR_EVENTS_MAP_ARRAY_LIST = "sensors"

        const val HEALTH_EVENT_MAP_PATH = "/health_event"
        const val HEALTH_EVENT_MAP_JSON = "json"

        const val MEASUREMENT_START_DATA_PATH = "/measurement_start_data"
        const val MEASUREMENT_START_DATA_JSON = "json"
    }
}