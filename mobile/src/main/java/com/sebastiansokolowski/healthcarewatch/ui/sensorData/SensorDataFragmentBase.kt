package com.sebastiansokolowski.healthcarewatch.ui.sensorData

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.dataModel.ChartData
import com.sebastiansokolowski.healthcarewatch.dataModel.StatisticData
import com.sebastiansokolowski.healthcarewatch.util.SensorAdapterItemHelper
import com.sebastiansokolowski.healthcarewatch.util.Utils
import com.sebastiansokolowski.healthcarewatch.view.DateValueFormatter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*

/**
 * Created by Sebastian Sokołowski on 10.03.20.
 */
open class SensorDataFragmentBase : DaggerFragment() {

    companion object {
        const val SENSOR_TYPE_KEY = "SENSOR_TYPE_KEY"
    }

    var sensorType: SensorAdapterItem = SensorAdapterItem.HEART_RATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val sensorTypeIndex = it.getInt(SENSOR_TYPE_KEY)
            sensorType = SensorAdapterItem.values()[sensorTypeIndex]
        }
    }

    fun fillChart(sensorAdapterItem: SensorAdapterItem, chartData: ChartData?) {
        if (chartData == null || chartData.xData.isEmpty() && chartData.yData.isEmpty() && chartData.zData.isEmpty()) {
            chart_lc.clear()
            return
        }

        val lineDataSetList = mutableListOf<LineDataSet>()

        var colorLineDataX = 0
        var colorLineDataY = 0
        var colorLineDataZ = 0

        context?.let { context ->
            colorLineDataX = ContextCompat.getColor(context, R.color.chart_x_color)
            colorLineDataY = ContextCompat.getColor(context, R.color.chart_y_color)
            colorLineDataZ = ContextCompat.getColor(context, R.color.chart_z_color)
        }

        //remove previous data
        if (statistics_table.childCount > 1) {
            statistics_table.removeViews(1, statistics_table.childCount - 1)
        }

        when (sensorAdapterItem) {
            SensorAdapterItem.LINEAR_ACCELERATION -> {
                val xLineDataSet = createLineDataSet(chartData.xData, "x", colorLineDataX)
                val yLineDataSet = createLineDataSet(chartData.yData, "y", colorLineDataY)
                val zLineDataSet = createLineDataSet(chartData.zData, "z", colorLineDataZ)

                lineDataSetList.add(xLineDataSet)
                lineDataSetList.add(yLineDataSet)
                lineDataSetList.add(zLineDataSet)

                addStatisticRow("x", chartData.xStatisticData)
                addStatisticRow("y", chartData.yStatisticData)
                addStatisticRow("z", chartData.zStatisticData)
            }
            else -> {
                val title = SensorAdapterItemHelper.getTitle(context, sensorAdapterItem)

                val xLineDataSet = createLineDataSet(chartData.xData, title, colorLineDataX)

                lineDataSetList.add(xLineDataSet)

                addStatisticRow("x", chartData.xStatisticData)
            }
        }

        chart_lc.xAxis.valueFormatter = DateValueFormatter()
        chart_lc.data = LineData(lineDataSetList.toList())
        chart_lc.notifyDataSetChanged()
        chart_lc.invalidate()
    }

    private fun addStatisticRow(title: String, statisticData: StatisticData) {
        val tableRow = TableRow(context)

        val titleTv = createStatisticTextView()
        val minValueTv = createStatisticTextView()
        val maxValueTv = createStatisticTextView()
        val averageValueTv = createStatisticTextView()

        titleTv.text = title
        minValueTv.text = Utils.format(statisticData.min, 2)
        maxValueTv.text = Utils.format(statisticData.max, 2)
        averageValueTv.text = Utils.format(statisticData.average, 2)

        tableRow.addView(titleTv)
        tableRow.addView(minValueTv)
        tableRow.addView(maxValueTv)
        tableRow.addView(averageValueTv)

        statistics_table.addView(tableRow)
    }

    private fun createStatisticTextView(): TextView {
        val textView = TextView(context)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)

        return textView
    }

    private fun createLineDataSet(data: MutableList<Entry>, label: String, color: Int): LineDataSet {
        val lineDataSet = LineDataSet(data, label)
        lineDataSet.setColor(color, 100)
        lineDataSet.setDrawCircles(false)
        lineDataSet.lineWidth = 2f

        return lineDataSet
    }

}