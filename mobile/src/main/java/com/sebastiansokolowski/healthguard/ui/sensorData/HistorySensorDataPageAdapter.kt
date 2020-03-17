package com.sebastiansokolowski.healthguard.ui.sensorData

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.sebastiansokolowski.healthguard.util.SensorAdapterItemHelper

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class HistorySensorDataPageAdapter(val context: Context?, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return HistorySensorDataFragment.newInstance(SensorAdapterItem.values()[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return SensorAdapterItemHelper.getTitle(context, SensorAdapterItem.values()[position])
    }

    override fun getCount() = SensorAdapterItem.values().size

}