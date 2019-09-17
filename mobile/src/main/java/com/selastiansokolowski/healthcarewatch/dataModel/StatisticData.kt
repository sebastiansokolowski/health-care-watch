package com.selastiansokolowski.healthcarewatch.dataModel

/**
 * Created by Sebastian Sokołowski on 16.09.19.
 */
data class StatisticData(var min: Float = Float.MAX_VALUE,
                         var max: Float = Float.MIN_VALUE,
                         var average: Float = 0f,
                         var sum: Float = 0f,
                         var count: Int = 0)