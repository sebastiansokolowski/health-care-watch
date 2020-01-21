package com.sebastiansokolowski.shared.dataModel

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class FallSettings(val threshold: Int = 0, val stepDetector: Boolean = false, val timeOfInactivity: Int = 0, val activityThreshold: Int = 0)