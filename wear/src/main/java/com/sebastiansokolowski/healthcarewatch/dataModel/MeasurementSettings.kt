package com.sebastiansokolowski.healthcarewatch.dataModel

import com.sebastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase

/**
 * Created by Sebastian Sokołowski on 23.08.19.
 */
data class MeasurementSettings(val samplingUs: Int, val fallThreshold: Int, val fallStepDetector: Boolean, val sensors: Set<Int>, val healthCareEngines: Set<HealthCareEngineBase>)