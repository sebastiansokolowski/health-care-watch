package com.selastiansokolowski.healthcarewatch.ui.sensorData

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class SensorDataPageAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return SensorDataFragment.newInstance(SENSOR_TYPE.values()[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return SENSOR_TYPE.values()[position].title
    }

    override fun getCount() = SENSOR_TYPE.values().size

    enum class SENSOR_TYPE(val title: String) {
        HEART_RATE("HEART RATE"),
        STEP_COUNTER("STEP COUNTER"),
        PRESSURE("PRESSURE"),
        GRAVITY("GRAVITY")

    }

}