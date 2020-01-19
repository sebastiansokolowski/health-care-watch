package com.sebastiansokolowski.healthcarewatch.ui.sensorData

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.dataModel.StatisticData
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import com.sebastiansokolowski.healthcarewatch.util.SafeCall
import com.sebastiansokolowski.healthcarewatch.util.SensorAdapterItemHelper
import com.sebastiansokolowski.healthcarewatch.util.Utils
import com.sebastiansokolowski.healthcarewatch.view.CustomMarkerView
import com.sebastiansokolowski.healthcarewatch.view.DateValueFormatter
import com.sebastiansokolowski.healthcarewatch.viewModel.HistoryDataViewModel
import com.sebastiansokolowski.healthcarewatch.viewModel.HistorySensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sensor_data_fragment.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class HistorySensorDataFragment : DaggerFragment() {

    companion object {
        private const val SENSOR_TYPE_KEY = "SENSOR_TYPE_KEY"

        fun newInstance(sensorAdapterItem: SensorAdapterItem): HistorySensorDataFragment {
            val fragment = HistorySensorDataFragment()

            val bundle = Bundle()
            bundle.putInt(SENSOR_TYPE_KEY, sensorAdapterItem.ordinal)
            fragment.arguments = bundle

            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var historySensorDataViewModel: HistorySensorDataViewModel
    private lateinit var historyDataViewModel: HistoryDataViewModel
    private var sensorAdapterItem: SensorAdapterItem = SensorAdapterItem.HEART_RATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val sensorTypeIndex = it.getInt(SENSOR_TYPE_KEY)
            sensorAdapterItem = SensorAdapterItem.values()[sensorTypeIndex]
        }
        historySensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HistorySensorDataViewModel::class.java)
        historyDataViewModel = ViewModelProviders.of(parentFragment!!, viewModelFactory)
                .get(HistoryDataViewModel::class.java)
        historySensorDataViewModel.setSensorType(sensorAdapterItem.sensorId)
        historySensorDataViewModel.refreshView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sensor_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        historyDataViewModel.currentDateLiveData.observe(this, Observer {
            it?.let {
                historySensorDataViewModel.setCurrentDate(it)
            }
        })
        historyDataViewModel.healthCareEventToShow.observe(this, Observer {
            it?.let {
                if (it.sensorEventData.target.type == sensorAdapterItem.sensorId) {
                    historySensorDataViewModel.showHealthCareEvent(it)
                    historyDataViewModel.healthCareEventToShow.postValue(null)
                }
            }
        })
        historySensorDataViewModel.showLoadingProgressBar.observe(this, Observer {
            val visibility = it ?: false

            if (visibility) {
                loading_pb.visibility = View.VISIBLE
                chart_lc.visibility = View.INVISIBLE
            } else {
                loading_pb.visibility = View.GONE
                chart_lc.visibility = View.VISIBLE
            }
        })
        historySensorDataViewModel.showStatisticsContainer.observe(this, Observer {
            val visibility = it ?: false

            if (visibility) {
                statistics_container.visibility = View.VISIBLE
            } else {
                statistics_container.visibility = View.INVISIBLE
            }
        })
        historySensorDataViewModel.healthCareEvents.observe(this, Observer {
            SafeCall.safeLet(context, it) { context, list ->
                val adapter = HealthCareEventAdapter(context, list, historySensorDataViewModel)
                adapter.setEmptyView(health_care_events_empty_view)
                health_care_events_lv.adapter = adapter
            }
        })
        historySensorDataViewModel.healthCareEventToRestore.observe(this, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    showRestoreDeletedItemSnackBar(it)
                }
            }
        })
        historySensorDataViewModel.healthCareEventSelected.observe(this, Observer {
            it?.let {
                historySensorDataViewModel.showHealthCareEvent(it)
            }
        })
        historySensorDataViewModel.entryHighlighted.observe(this, Observer {
            it?.let {
                highlightValue(it)
            }
        })
        initChart(sensorAdapterItem)
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

    private fun highlightValue(entry: Entry) {
        chart_lc.data?.let { lineData ->
            lineData.dataSets?.let {
                chart_lc.centerViewToAnimated(entry.x, entry.y, YAxis.AxisDependency.LEFT, TimeUnit.SECONDS.toMillis(1))
                chart_lc.highlightValue(entry.x, entry.y, 0)
            }
        }
    }

    private fun initChart(sensorAdapterItem: SensorAdapterItem) {
        context?.let {
            val marker = CustomMarkerView(it, R.layout.custom_marker_view)
            marker.chartView = chart_lc
            chart_lc.marker = marker
        }
        chart_lc.setTouchEnabled(true)

        historySensorDataViewModel.chartLiveData.observe(this, Observer {
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
                SensorAdapterItem.ACCELEROMETER,
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

            historySensorDataViewModel.entryHighlighted.value?.let {
                highlightValue(it)
            }
        })
    }

    private fun showRestoreDeletedItemSnackBar(healthCareEvent: HealthCareEvent) {
        view?.let {
            val snackbar = Snackbar.make(it, getString(R.string.restore_deleted_item_title), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.action_undo)) {
                historySensorDataViewModel.restoreDeletedEvent(healthCareEvent)
            }
            snackbar.show()
        }
    }

}