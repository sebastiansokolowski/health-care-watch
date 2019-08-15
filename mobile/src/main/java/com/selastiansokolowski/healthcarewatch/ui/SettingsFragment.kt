package com.selastiansokolowski.healthcarewatch.ui

import android.Manifest
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import android.widget.Toast
import com.selastiansokolowski.healthcarewatch.R
import com.selastiansokolowski.healthcarewatch.view.preference.CustomMultiSelectListPreference
import com.selastiansokolowski.healthcarewatch.view.preference.TimePickerPreference
import com.selastiansokolowski.healthcarewatch.view.preference.TimePickerPreferenceDialogFragment
import com.selastiansokolowski.healthcarewatch.viewModel.SettingsViewModel
import com.selastiansokolowski.shared.SettingsSharedPreferences
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
    lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return childFragmentInjector
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        setPreferencesFromResource(R.xml.settings_fragment, p1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settingsViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        var dialogFragment: DialogFragment? = null
        when (preference) {
            is TimePickerPreference -> {
                dialogFragment = TimePickerPreferenceDialogFragment()
            }
            is CustomMultiSelectListPreference -> {
                when (preference.key) {
                    SettingsSharedPreferences.CONTACTS -> {
                        if (!checkPermissions()) {
                            requestPermissions()
                            return
                        } else {
                            settingsViewModel.setupPreference(preference)
                        }
                    }
                    SettingsSharedPreferences.HEALTH_CARE_EVENTS -> {
                        if (settingsViewModel.setupModel.setupComplete.value != true) {
                            Toast.makeText(context, "Please setup watch earlier.", Toast.LENGTH_LONG).show()
                        } else {
                            settingsViewModel.setupPreference(preference)
                        }
                    }
                }
            }
        }

        if (dialogFragment != null && preference != null) {
            val bundle = Bundle(1)
            bundle.putString("key", preference.key)
            dialogFragment.arguments = bundle
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(fragmentManager, null)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun checkPermissions(): Boolean {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        return false
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity?.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS), 0)
        }
    }

    //SharedPreferences.OnSharedPreferenceChangeListener

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            settingsViewModel.onSharedPreferenceChanged(key)
        }
    }
}