package com.selastiansokolowski.healthcarewatch.view

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class DateValueFormatter : IAxisValueFormatter {
    var dtf1 = SimpleDateFormat("yyyyy-mm-dd")
    var dtf2 = SimpleDateFormat("HH:mm:ss")


    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        val timestampFromMidnight = value.toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.timeInMillis += timestampFromMidnight

        return dtf2.format(calendar.time)
    }
}