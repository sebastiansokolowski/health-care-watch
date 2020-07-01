package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class FallSettings(val threshold: Int = 25, val samplingTimeS: Int = 6,
                        val inactivityDetector: Boolean = true, val inactivityDetectorTimeoutS: Int = 1, val inactivityDetectorThreshold: Int = 5)