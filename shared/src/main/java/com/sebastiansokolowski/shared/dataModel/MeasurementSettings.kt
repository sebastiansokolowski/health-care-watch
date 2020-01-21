package com.sebastiansokolowski.shared.dataModel

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class MeasurementSettings(val samplingUs: Int = 1000, val healthCareEvents: ArrayList<String> = ArrayList(), val fallSettings: FallSettings = FallSettings())