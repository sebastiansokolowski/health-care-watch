package com.selastiansokolowski.healthcarewatch.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.charts.LineChart

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class CustomLineChart : LineChart {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        requestDisallowInterceptTouchEvent(true)
        return super.onTouchEvent(event)
    }
}