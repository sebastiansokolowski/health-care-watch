package com.sebastiansokolowski.healthguard.ui

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sebastiansokolowski.healthguard.MainActivity
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.model.SetupModel
import com.sebastiansokolowski.healthguard.view.preference.CustomMultiSelectListPreference
import com.sebastiansokolowski.healthguard.viewModel.SettingsViewModel
import com.sebastiansokolowski.shared.SettingsSharedPreferences
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SettingsFragment : PreferenceFragmentCompat(), HasSupportFragmentInjector, SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var childFragmentInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        activity?.title = getString(R.string.settings_title)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return childFragmentInjector
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        setPreferencesFromResource(R.xml.settings_fragment, p1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settingsViewModel = ViewModelProvider(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.refreshView.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    refreshView()
                }
            }
        })
        settingsViewModel.showSelectContactDialog.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled().let {
                it?.let {
                    val contactDialogPreference: Preference? = findPreference(SettingsSharedPreferences.CONTACTS)
                    onDisplayPreferenceDialog(contactDialogPreference)
                }
            }
        })

        val locationPreference: Preference? = findPreference(SettingsSharedPreferences.SMS_USER_LOCATION)
        locationPreference?.setOnPreferenceChangeListener { preference, any ->
            if (!checkLocationPermissions()) {
                requestLocationPermissions()
                false
            } else {
                true
            }
        }
        val smsPreference: Preference? = findPreference(SettingsSharedPreferences.SMS_NOTIFICATIONS)
        smsPreference?.setOnPreferenceChangeListener { preference, any ->
            if (!checkSendSMSPermissions()) {
                requestSMSPermissions()
                false
            } else {
                true
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == "advanced_settings") {
            val mainActivity: MainActivity = activity as MainActivity
            mainActivity.showFragment(AdvancedSettingsFragment(), true)
        }

        return super.onPreferenceTreeClick(preference)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is CustomMultiSelectListPreference -> {
                when (preference.key) {
                    SettingsSharedPreferences.CONTACTS -> {
                        if (!checkContactsPermissions()) {
                            requestContactsPermissions()
                            return
                        } else {
                            settingsViewModel.setupPreference(preference)
                        }
                    }
                    SettingsSharedPreferences.HEALTH_EVENTS -> {
                        if (settingsViewModel.setupModel.setupComplete.value != SetupModel.SetupStep.COMPLETED) {
                            Toast.makeText(context, getString(R.string.settings_health_events_not_synced), Toast.LENGTH_LONG).show()
                        } else {
                            settingsViewModel.setupPreference(preference)
                        }
                    }
                }
            }
        }

        super.onDisplayPreferenceDialog(preference)
    }

    private fun checkContactsPermissions(): Boolean {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        return false
    }

    private fun checkSendSMSPermissions(): Boolean {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        return false
    }

    private fun checkLocationPermissions(): Boolean {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        return false
    }

    private fun requestContactsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 0)
        }
    }

    private fun requestSMSPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 1)
        }
    }

    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }
    }

    //SharedPreferences.OnSharedPreferenceChangeListener

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            settingsViewModel.onSharedPreferenceChanged(key)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        settingsViewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun refreshView() {
        val mainActivity = activity as MainActivity?
        mainActivity?.showFragment(SettingsFragment())
    }
}