package com.selastiansokolowski.healthcarewatch.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.charts.LineChart
import com.selastiansokolowski.healthcarewatch.R

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class CustomLineChart : LineChart {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        setNoDataText(context.getString(R.string.sensor_data_chart_no_data))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        requestDisallowInterceptTouchEvent(true)
        return super.onTouchEvent(event)
    }
}