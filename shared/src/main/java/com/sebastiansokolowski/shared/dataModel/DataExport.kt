package com.sebastiansokolowski.shared.dataModel

data class DataExport(val healthEvents: MutableList<HealthEvent>, val sensorEvents: MutableList<SensorEvent>)