package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.dataModel.StatisticData
import com.selastiansokolowski.healthcarewatch.util.SensorAdapterItemHelper
import com.selastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.selastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.selastiansokolowski.healthcarewatch.viewModel.LiveSensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class LiveSensorDataFragment : DaggerFragment() {

    companion object {
        private const val SENSOR_TYPE_KEY = "SENSOR_TYPE_KEY"

        fun newInstance(sensorTYPE: SensorAdapterItem): LiveSensorDataFragment {
            val fragment = LiveSensorDataFragment()

            val bundle = Bundle()
            bundle.putInt(SENSOR_TYPE_KEY, sensorTYPE.ordinal)
            fragment.arguments = bundle

            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var liveSensorDataViewModel: LiveSensorDataViewModel
    private var sensorType: SensorAdapterItem = SensorAdapterItem.HEART_RATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val sensorTypeIndex = it.getInt(SENSOR_TYPE_KEY)
            sensorType = SensorAdapterItem.values()[sensorTypeIndex]
        }

        liveSensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(LiveSensorDataViewModel::class.java)
        liveSensorDataViewModel.initLiveData(sensorType.sensorId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        liveSensorDataViewModel.showStatisticsContainer.observe(this, Observer {
            val visibility = it ?: false

            if (visibility) {
                statistics_container.visibility = View.VISIBLE
            } else {
                statistics_container.visibility = View.INVISIBLE
            }
        })
        initChart(sensorType)
    }

    private fun addStatisticRow(title: String, statisticData: StatisticData) {
        val tableRow = TableRow(context)

        val titleTv = createStatisticTextView()
        val minValueTv = createStatisticTextView()
        val maxValueTv = createStatisticTextView()
        val averageValueTv = createStatisticTextView()

        titleTv.text = title
        minValueTv.text = statisticData.min.toString()
        maxValueTv.text = statisticData.max.toString()
        averageValueTv.text = statisticData.average.toString()

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

    private fun initChart(sensorAdapterItem: SensorAdapterItem) {
        context?.let {
            val marker = CustomMarkerView(it, R.layout.custom_marker_view)
            marker.chartView = chart_lc
            chart_lc.marker = marker
        }
        chart_lc.setTouchEnabled(true)

        liveSensorDataViewModel.chartLiveData.observe(this, Observer {
            if (it == null || it.xData.isEmpty() && it.yData.isEmpty() && it.zData.isEmpty()) {
                chart_lc.clear()
                return@Observer
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
                SensorAdapterItem.GRAVITY,
                SensorAdapterItem.LINEAR_ACCELERATION -> {
                    val xLineDataSet = LineDataSet(it.xData, "x")
                    xLineDataSet.setColor(colorLineDataX, 100)
                    xLineDataSet.setCircleColor(colorLineDataX)

                    val yLineDataSet = LineDataSet(it.yData, "y")
                    yLineDataSet.setColor(colorLineDataY, 100)
                    yLineDataSet.setCircleColor(colorLineDataY)

                    val zLineDataSet = LineDataSet(it.zData, "z")
                    zLineDataSet.setColor(colorLineDataZ, 100)
                    zLineDataSet.setCircleColor(colorLineDataZ)

                    lineDataSetList.add(xLineDataSet)
                    lineDataSetList.add(yLineDataSet)
                    lineDataSetList.add(zLineDataSet)

                    addStatisticRow("x", it.xStatisticData)
                    addStatisticRow("y", it.yStatisticData)
                    addStatisticRow("z", it.zStatisticData)
                }
                else -> {
                    val title = SensorAdapterItemHelper.getTitle(context, sensorAdapterItem)

                    val xLineDataSet = LineDataSet(it.xData, title)
                    xLineDataSet.setColor(colorLineDataX, 100)
                    xLineDataSet.setCircleColor(colorLineDataX)

                    lineDataSetList.add(xLineDataSet)

                    addStatisticRow("x", it.xStatisticData)
                }
            }

            chart_lc.xAxis.valueFormatter = DateValueFormatter()
            chart_lc.data = LineData(lineDataSetList.toList())
            chart_lc.notifyDataSetChanged()
            chart_lc.invalidate()
        })
    }

}