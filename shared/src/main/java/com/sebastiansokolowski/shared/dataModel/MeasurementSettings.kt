package com.sebastiansokolowski.shared.dataModel

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class MeasurementSettings(val samplingUs: Int, val healthCareEvents: ArrayList<String>, val fallSettings: FallSettings)