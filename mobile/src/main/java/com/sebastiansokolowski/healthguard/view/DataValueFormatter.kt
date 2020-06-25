package com.sebastiansokolowski.healthguard.view

import com.github.mikephil.charting.formatter.ValueFormatter
import com.sebastiansokolowski.shared.util.Utils

/**
 * Created by Sebastian Sokołowski on 14.03.20.
 */
class DataValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return Utils.formatValue(value, 1)
    }
}