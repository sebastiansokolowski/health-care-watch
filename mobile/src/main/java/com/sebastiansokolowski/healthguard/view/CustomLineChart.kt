package com.sebastiansokolowski.healthguard.view

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.charts.LineChart
import com.sebastiansokolowski.healthguard.R

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class CustomLineChart : LineChart {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        setNoDataText(context.getString(R.string.sensor_data_chart_history_no_data))
        setNoDataTextColor(ContextCompat.getColor(context, R.color.chart_no_data_info_color))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        requestDisallowInterceptTouchEvent(true)
        return super.onTouchEvent(event)
    }
}