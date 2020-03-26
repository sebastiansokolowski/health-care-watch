package com.sebastiansokolowski.healthguard.view

import com.github.mikephil.charting.formatter.ValueFormatter
import com.sebastiansokolowski.shared.util.Utils
import kotlin.math.abs

/**
 * Created by Sebastian Sokołowski on 14.03.20.
 */
class DataValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return if (abs(value) <= 0.1f) {
            ""
        } else {
            return Utils.format(value, 2)
        }
    }
}