package com.sebastiansokolowski.healthguard.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.MainActivity
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.model.SetupModel
import com.sebastiansokolowski.healthguard.ui.adapter.HealthEventAdapter
import com.sebastiansokolowski.healthguard.ui.dialog.HealthEventDetailsDialogFragment
import com.sebastiansokolowski.healthguard.util.SafeCall
import com.sebastiansokolowski.healthguard.util.SingleEvent
import com.sebastiansokolowski.healthguard.view.CustomSnackbar
import com.sebastiansokolowski.healthguard.viewModel.HomeViewModel
import com.sebastiansokolowski.shared.dataModel.DataExport
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

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.app_name)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeViewModel = ViewModelProvider(this, viewModelFactory)
                .get(HomeViewModel::class.java)
        homeViewModel.measurementState.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    measurement_btn.text = getString(R.string.measurement_stop_btn)
                } else {
                    measurement_btn.text = getString(R.string.measurement_start_btn)
                }
            }
        })
        homeViewModel.heartRate.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                heart_rate_tv.text = "---"
            } else {
                heart_rate_tv.text = it
                heart_rate_iv.startAnimation()
            }
        })
        homeViewModel.healthEvents.observe(viewLifecycleOwner, Observer {
            SafeCall.safeLet(context, it) { context, list ->
                val adapter = HealthEventAdapter(context, list, homeViewModel)
                adapter.setEmptyView(health_events_empty_view)
                health_events_lv.adapter = adapter
            }
        })
        homeViewModel.healthEventDetails.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    val mainActivity: MainActivity = activity as MainActivity
                    mainActivity.showDialog(HealthEventDetailsDialogFragment.newInstance(it))
                }
            }
        })
        homeViewModel.healthEventToRestore.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    showRestoreDeletedItemSnackBar(it)
                }
            }
        })

        measurement_btn.setOnClickListener {
            homeViewModel.toggleMeasurementState()
        }

        homeViewModel.healthEventSelected.observe(viewLifecycleOwner, Observer {
            it?.let {
                showHealthEventInHistoryFragment(it)
            }
        })
        homeViewModel.setupState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    SetupModel.SetupStatus.CONNECTING -> {
                        measurement_btn.isEnabled = false
                        measurement_btn.text = getString(R.string.setup_connecting_btn)
                    }
                    SetupModel.SetupStatus.SYNC_DATA -> {
                        measurement_btn.text = getString(R.string.measurement_sync_data_btn)
                    }
                    SetupModel.SetupStatus.COMPLETED -> {
                        measurement_btn.isEnabled = true
                    }
                }
            }
        })
        homeViewModel.filesToShare.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    showShareScreen(it)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (BuildConfig.DEBUG) {
            inflater.inflate(R.menu.home_menu_debug, menu)
        } else {
            inflater.inflate(R.menu.home_menu_release, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                showLicencesDialog()
                return true
            }
            R.id.share_data_for_testing -> {
                showShareDataDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showShareDataDialog() {
        context?.let {
            val builder = AlertDialog.Builder(it)
            val view = requireActivity().layoutInflater.inflate(R.layout.share_data, null)

            val rgTestModeView = view.findViewById<RadioGroup>(R.id.rg_test_mode)
            val etCommentView = view.findViewById<EditText>(R.id.et_comment)
            val etCounterView = view.findViewById<EditText>(R.id.et_events_number)
            rgTestModeView.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_test_mode_general -> {
                        etCounterView.visibility = View.GONE
                    }
                    else -> {
                        etCounterView.visibility = View.VISIBLE
                    }
                }
            }
            val dialog = builder.setView(view)
                    .setPositiveButton(R.string.action_ok) { _, _ -> }
                    .setNegativeButton(R.string.action_cancel) { _, _ -> }.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val comment = etCommentView.text.toString()
                val counter = etCounterView.text.toString().toIntOrNull()
                val adlMode = when (rgTestModeView.checkedRadioButtonId) {
                    R.id.rb_test_mode_general -> {
                        DataExport.TestMode.GENERAL
                    }
                    R.id.rb_test_mode_fall -> {
                        DataExport.TestMode.FALL
                    }
                    R.id.rb_test_mode_convulsions -> {
                        DataExport.TestMode.CONVULSIONS
                    }
                    R.id.rb_test_mode_adl -> {
                        DataExport.TestMode.ADL
                    }
                    else -> {
                        null
                    }
                }

                if (adlMode == null) {
                    Toast.makeText(context, "Please select test mode", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (adlMode != DataExport.TestMode.GENERAL && counter == null) {
                    Toast.makeText(context, "Please fill in the counter field", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                homeViewModel.shareDataForTesting(comment, adlMode, counter)
                dialog.dismiss()
            }
        }
    }

    private fun showShareScreen(uris: ArrayList<Uri>) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            type = "text/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(shareIntent)
    }

    @Suppress("DEPRECATION")
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

    private fun showRestoreDeletedItemSnackBar(healthEventEntity: HealthEventEntity) {
        view?.let {
            val snackbar = CustomSnackbar(it.context).make(it, getString(R.string.restore_deleted_item_title), Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.action_undo)) {
                homeViewModel.restoreDeletedEvent(healthEventEntity)
            }
            snackbar.show()
        }
    }

    private fun showHealthEventInHistoryFragment(healthEventEntity: HealthEventEntity) {
        val mainActivity: MainActivity = activity as MainActivity
        mainActivity.showFragment(HistoryDataFragment())
        mainActivity.healthEventEntitySelected.postValue(SingleEvent(healthEventEntity))
    }
}