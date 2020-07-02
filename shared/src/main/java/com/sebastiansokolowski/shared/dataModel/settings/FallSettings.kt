package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class FallSettings(val threshold: Int = 28, val samplingTimeS: Int = 6,
                        val inactivityDetector: Boolean = true, val inactivityDetectorTimeoutS: Int = 2, val inactivityDetectorThreshold: Int = 3)