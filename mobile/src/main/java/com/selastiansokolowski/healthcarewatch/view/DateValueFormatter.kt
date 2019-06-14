package com.selastiansokolowski.healthcarewatch.view

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.selastiansokolowski.healthcarewatch.util.EntryHelper

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class DateValueFormatter : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return EntryHelper.getDate(value)
    }
}