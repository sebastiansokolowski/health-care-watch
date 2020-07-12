package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class EpilepsySettings(val threshold: Int = 6, val samplingTimeS: Int = 7, val motions: Int = 100)