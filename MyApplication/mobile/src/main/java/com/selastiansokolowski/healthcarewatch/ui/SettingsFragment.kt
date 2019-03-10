package com.selastiansokolowski.healthcarewatch.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.selastiansokolowski.healthcarewatch.R

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }
}