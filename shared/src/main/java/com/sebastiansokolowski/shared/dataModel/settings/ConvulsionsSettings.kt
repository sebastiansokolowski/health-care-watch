package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class ConvulsionsSettings(val threshold: Int = 5, val samplingTimeS: Int = 5, val motionsToDetect: Int = 65, val motionsToCancel: Int = 30)