package com.sebastiansokolowski.healthguard.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import android.view.View
import com.sebastiansokolowski.healthguard.MainActivity
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.viewModel.AdvancedSettingsViewModel
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class AdvancedSettingsFragment : PreferenceFragmentCompat(), HasSupportFragmentInjector, SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = javaClass.canonicalName

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var childFragmentInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>

    private lateinit var advancedSettingsViewModel: AdvancedSettingsViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        setDisplayHomeAsUpEnabled(true)
        activity?.title = getString(R.string.advanced_settings_title)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        setDisplayHomeAsUpEnabled(false)
    }

    private fun setDisplayHomeAsUpEnabled(enabled: Boolean) {
        val mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

    override fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return childFragmentInjector
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        setPreferencesFromResource(R.xml.advanced_settings_fragment, p1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        advancedSettingsViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(AdvancedSettingsViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
    }


    //SharedPreferences.OnSharedPreferenceChangeListener

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            advancedSettingsViewModel.onSharedPreferenceChanged(key)
        }
    }
}