package com.selastiansokolowski.healthcarewatch.ui

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.ui.sensorData.SensorDataPageAdapter
import com.selastiansokolowski.healthcarewatch.viewModel.SensorDataViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.history_data_fragment.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HistoryDataFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var sensorDataViewModel: SensorDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.history_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensorDataViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SensorDataViewModel::class.java)

        sensor_vp.adapter = SensorDataPageAdapter(childFragmentManager)
        sensor_data_tl.setupWithViewPager(sensor_vp)

        sensorDataViewModel.currentDateLiveData.observe(this, Observer {
            it?.let { date ->
                val dateTimeFormatter = SimpleDateFormat("yyyy/MM/dd")
                current_date_tv.text = dateTimeFormatter.format(date)

                sensorDataViewModel.initHistoryData(date)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.sensor_data_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.date -> {
                showSelectDateDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSelectDateDialog() {
        val listener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            sensorDataViewModel.currentDateLiveData.postValue(calendar.time)
            sensorDataViewModel.initHistoryData(calendar.time)
        }

        context?.let {
            val calendar = Calendar.getInstance()
            calendar.time = sensorDataViewModel.currentDateLiveData.value
            val dialog = DatePickerDialog(it, listener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }
    }
}