package com.selastiansokolowski.healthcarewatch.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import com.selastiansokolowski.healthcarewatch.MainActivity
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.model.SetupModel
import com.selastiansokolowski.healthcarewatch.ui.adapter.HealthCareEventAdapter
import com.selastiansokolowski.healthcarewatch.util.SafeCall
import com.selastiansokolowski.healthcarewatch.util.SingleEvent
import com.selastiansokolowski.healthcarewatch.viewModel.HomeViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.home_fragment.*
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeViewModel::class.java)
        homeViewModel.measurementState.observe(this, Observer {
            heart_rate_tv.text = "---"
            it?.let {
                if (it) {
                    measurement_btn.text = getString(R.string.measurement_stop_btn)
                } else {
                    measurement_btn.text = getString(R.string.measurement_start_btn)
                }
            }
        })
        homeViewModel.heartRate.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                heart_rate_tv.text = "---"
            } else {
                heart_rate_tv.text = it
                heart_rate_iv.startAnimation()
            }
        })
        homeViewModel.healthCareEvents.observe(this, Observer {
            SafeCall.safeLet(context, it) { context, list ->
                val adapter = HealthCareEventAdapter(context, list, homeViewModel)
                adapter.setEmptyView(health_care_events_empty_view)
                health_care_events_lv.adapter = adapter
            }
        })
        homeViewModel.healthCareEventToRestore.observe(this, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    showRestoreDeletedItemSnackBar(it)
                }
            }
        })

        measurement_btn.setOnClickListener {
            homeViewModel.toggleMeasurementState()
        }

        homeViewModel.healthCareEventSelected.observe(this, Observer {
            it?.let {
                showHealthCareEventInHistoryFragment(it)
            }
        })
        homeViewModel.setupState.observe(this, Observer {
            it?.let {
                when (it) {
                    SetupModel.SetupStep.CONNECTING -> {
                        measurement_btn.isEnabled = false
                        measurement_btn.text = getString(R.string.setup_connecting_btn)
                    }
                    SetupModel.SetupStep.SYNC_DATA -> {
                        measurement_btn.text = getString(R.string.measurement_sync_data_btn)
                    }
                    SetupModel.SetupStep.COMPLETED -> {
                        measurement_btn.isEnabled = true
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.about -> {
                showLicencesDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLicencesDialog() {
        context?.let {
            val message = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(getString(R.string.licences), FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(getString(R.string.licences))
            }

            val dialog: AlertDialog = AlertDialog.Builder(it)
                    .setTitle(getString(R.string.dialog_licences_title))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.action_ok), null)
                    .show()

            dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun showRestoreDeletedItemSnackBar(healthCareEvent: HealthCareEvent) {
        view?.let {
            val snackbar = Snackbar.make(it, getString(R.string.restore_deleted_item_title), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.action_undo)) {
                homeViewModel.restoreDeletedEvent(healthCareEvent)
            }
            snackbar.show()
        }
    }

    private fun showHealthCareEventInHistoryFragment(healthCareEvent: HealthCareEvent) {
        val mainActivity: MainActivity = activity as MainActivity
        mainActivity.showFragment(HistoryDataFragment())
        mainActivity.healthCareEventSelected.postValue(SingleEvent(healthCareEvent))
    }
}