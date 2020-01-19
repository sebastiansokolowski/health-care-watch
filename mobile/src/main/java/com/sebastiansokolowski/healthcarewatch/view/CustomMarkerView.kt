package com.sebastiansokolowski.healthcarewatch.view

import android.content.Context
import android.view.View
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.sebastiansokolowski.healthcarewatch.util.EntryHelper
import com.sebastiansokolowski.healthcarewatch.util.SafeCall
import com.sebastiansokolowski.healthcarewatch.util.Utils
import kotlinx.android.synthetic.main.custom_marker_view.view.*

/**
 * Created by Sebastian Sokołowski on 17.03.19.
 */
class CustomMarkerView : MarkerView {
    constructor(context: Context) : super(context, 0)

    constructor(context: Context?, layoutResource: Int) : super(context, layoutResource)


    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        SafeCall.safeLet(e?.x, e?.y) { x, y ->
            text.text = "${EntryHelper.getDate(x)}\n${Utils.format(y, 2)}"
            text.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}