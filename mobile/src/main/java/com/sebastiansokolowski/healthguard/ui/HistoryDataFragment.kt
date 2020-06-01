package com.sebastiansokolowski.healthguard.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sebastiansokolowski.healthguard.MainActivity
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.ui.sensorData.HistorySensorDataPageAdapter
import com.sebastiansokolowski.healthguard.viewModel.HistoryDataViewModel
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

    private lateinit var historyDataViewModel: HistoryDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.history_data_title)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.history_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        historyDataViewModel = ViewModelProvider(this, viewModelFactory)
                .get(HistoryDataViewModel::class.java)

        sensor_vp.adapter = HistorySensorDataPageAdapter(context, childFragmentManager)
        sensor_data_tl.setupWithViewPager(sensor_vp)

        historyDataViewModel.viewPagerToShow.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    if (sensor_vp.currentItem != it) {
                        sensor_vp.setCurrentItem(it, true)
                    }
                }
            }
        })

        historyDataViewModel.currentDateLiveData.observe(this, Observer {
            it?.let { date ->
                val dateTimeFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                current_date_tv.text = dateTimeFormatter.format(date)

                setCurrentDateNextBtnEnable(!DateUtils.isToday(date.time))
            }
        })
        current_date_prev_btn.setOnClickListener {
            historyDataViewModel.decreaseCurrentDate()
        }
        current_date_next_btn.setOnClickListener {
            historyDataViewModel.increaseCurrentDate()
        }

        val mainActivity: MainActivity = activity as MainActivity
        mainActivity.healthEventEntitySelected.observe(this, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    historyDataViewModel.showHealthEvent(it)
                }
            }
        })
    }

    private fun setCurrentDateNextBtnEnable(enable: Boolean) {
        current_date_next_btn.isEnabled = enable
        current_date_next_btn.alpha = if (enable) 1f else 0.2f
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sensor_data_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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

            historyDataViewModel.currentDateLiveData.postValue(calendar.time)
        }

        context?.let {
            val calendar = Calendar.getInstance()
            calendar.time = historyDataViewModel.currentDateLiveData.value
            val dialog = DatePickerDialog(it, listener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }
    }
}