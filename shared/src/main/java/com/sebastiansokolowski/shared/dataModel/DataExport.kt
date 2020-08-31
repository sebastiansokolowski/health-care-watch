package com.sebastiansokolowski.shared.dataModel

data class DataExport(val comment: String, val testMode: TestMode, val counter: Int?, val healthEvents: MutableList<HealthEvent>, val sensorEvents: MutableList<SensorEvent>) {
    enum class TestMode {
        FALL,
        CONVULSIONS,
        ADL,
        GENERAL
    }
}
