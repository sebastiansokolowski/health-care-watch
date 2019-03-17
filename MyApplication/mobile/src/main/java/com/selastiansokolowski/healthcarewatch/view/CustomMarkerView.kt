package com.selastiansokolowski.healthcarewatch.view

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.custom_marker_view.view.*

/**
 * Created by Sebastian Sokołowski on 17.03.19.
 */
class CustomMarkerView(context: Context, layoutId: Int) : MarkerView(context, layoutId) {
    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        text.text = e?.y.toString()

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}