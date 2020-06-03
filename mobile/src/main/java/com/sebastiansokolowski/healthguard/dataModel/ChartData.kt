package com.sebastiansokolowski.healthguard.dataModel

import com.github.mikephil.charting.data.LineDataSet

/**
 * Created by Sebastian Sokołowski on 09.09.19.
 */
data class ChartData(
        //x
        var xData: MutableList<LineDataSet> = mutableListOf(),
        var xStatisticData: StatisticData = StatisticData(),
        //y
        var yData: MutableList<LineDataSet> = mutableListOf(),
        var yStatisticData: StatisticData = StatisticData(),
        //z
        var zData: MutableList<LineDataSet> = mutableListOf(),
        var zStatisticData: StatisticData = StatisticData())