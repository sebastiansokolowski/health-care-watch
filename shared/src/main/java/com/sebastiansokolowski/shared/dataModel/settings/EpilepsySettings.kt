package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 22.01.20.
 */
data class EpilepsySettings(val threshold: Int = 3, val samplingTimeS: Int = 30, val percentOfPositiveSignals: Int = 97)