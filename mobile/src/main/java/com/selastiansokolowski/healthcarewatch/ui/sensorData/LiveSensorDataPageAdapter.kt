package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class LiveSensorDataPageAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return LiveSensorDataFragment.newInstance(SensorAdapterItem.values()[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return SensorAdapterItem.values()[position].title
    }

    override fun getCount() = SensorAdapterItem.values().size

}