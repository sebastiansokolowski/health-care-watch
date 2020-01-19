package com.sebastiansokolowski.healthcarewatch.dataModel

/**
 * Created by Sebastian Sokołowski on 23.08.19.
 */
data class MeasurementSettings(val samplingUs: Int, val fallThreshold: Int, val fallStepDetector: Boolean, val healthCareEvents: ArrayList<String>)