package com.sebastiansokolowski.healthguard.ui.sensorData

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sebastiansokolowski.healthguard.util.SensorAdapterItemHelper

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class LiveSensorDataPageAdapter(val context: Context?, fragmentManager: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return LiveSensorDataFragment.newInstance(SensorAdapterItem.values()[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return SensorAdapterItemHelper.getTitle(context, SensorAdapterItem.values()[position])
    }

    override fun getCount() = SensorAdapterItem.values().size

}