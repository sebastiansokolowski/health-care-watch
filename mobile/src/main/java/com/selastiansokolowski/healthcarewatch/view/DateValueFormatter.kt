package com.selastiansokolowski.healthcarewatch.view

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class DateValueFormatter : IAxisValueFormatter {
    var dtf1 = SimpleDateFormat("yyyyy-mm-dd")
    var dtf2 = SimpleDateFormat("HH:mm:ss")

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = value.toLong()

        return dtf2.format(calendar.time)
    }
}