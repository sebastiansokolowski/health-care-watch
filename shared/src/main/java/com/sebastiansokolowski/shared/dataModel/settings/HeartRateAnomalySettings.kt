package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class HeartRateAnomalySettings(val stepDetectorTimeoutInMin: Int = 5, val minThreshold: Int = 40, val maxThresholdDuringInactivity: Int = 120, val maxThresholdDuringActivity: Int = 150)

