package com.sebastiansokolowski.healthcarewatch.view

import com.github.mikephil.charting.formatter.ValueFormatter
import com.sebastiansokolowski.healthcarewatch.util.EntryHelper

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class DateValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return EntryHelper.getDate(value)
    }
}