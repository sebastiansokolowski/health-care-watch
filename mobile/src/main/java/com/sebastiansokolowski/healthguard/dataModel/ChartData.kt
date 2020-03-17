package com.sebastiansokolowski.healthguard.dataModel

import com.github.mikephil.charting.data.Entry

/**
 * Created by Sebastian Sokołowski on 09.09.19.
 */
data class ChartData(
        //x
        var xData: MutableList<Entry> = mutableListOf(),
        var xStatisticData: StatisticData = StatisticData(),
        //y
        var yData: MutableList<Entry> = mutableListOf(),
        var yStatisticData: StatisticData = StatisticData(),
        //z
        var zData: MutableList<Entry> = mutableListOf(),
        var zStatisticData: StatisticData = StatisticData())