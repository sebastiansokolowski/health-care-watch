package com.selastiansokolowski.healthcarewatch.dataModel

import com.selastiansokolowski.healthcarewatch.model.healthCare.HealthCareEngineBase

/**
 * Created by Sebastian Sokołowski on 23.08.19.
 */
data class MeasurementSettings(val samplingUs: Int, val sensors: Set<Int>, val healthCareEngines: Set<HealthCareEngineBase>)