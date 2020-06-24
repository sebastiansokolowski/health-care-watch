package com.sebastiansokolowski.healthguard.dataModel

import com.github.mikephil.charting.data.LineDataSet

/**
 * Created by Sebastian Sokołowski on 09.09.19.
 */
data class ChartData(
        var xData: MutableList<LineDataSet> = mutableListOf(),
        var xStatisticData: StatisticData = StatisticData()
)