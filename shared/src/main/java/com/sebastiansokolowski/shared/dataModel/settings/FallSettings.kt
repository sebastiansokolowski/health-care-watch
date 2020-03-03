package com.sebastiansokolowski.shared.dataModel.settings

/**
 * Created by Sebastian Sokołowski on 20.01.20.
 */
data class FallSettings(val threshold: Int = 20, val stepDetector: Boolean = true, val timeOfInactivity: Int = 0, val activityThreshold: Int = 3)