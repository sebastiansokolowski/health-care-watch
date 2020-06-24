package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class FallSettings(val threshold: Int = 18, val sampleCount: Int = 200,
                        val stepDetector: Boolean = true, val stepDetectorTimeoutS: Int = 10,
                        val inactivityDetector: Boolean = true, val inactivityDetectorTimeoutS: Int = 2, val inactivityDetectorThreshold: Int = 5)