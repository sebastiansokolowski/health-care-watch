package com.selastiansokolowski.healthcarewatch.view.preference

import android.content.Context
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.TimePicker

/**
 * Created by Sebastian Sokołowski on 26.04.19.
 */
class TimePickerPreferenceDialogFragment : PreferenceDialogFragmentCompat(), DialogPreference.TargetFragment {

    private var timePicker: TimePicker? = null

    override fun onCreateDialogView(context: Context?): View {
        val timePicker = TimePicker(context)
        this.timePicker = timePicker
        return timePicker
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        timePicker?.let {
            it.setIs24HourView(true)
            val timePreference: TimePickerPreference = preference as TimePickerPreference
            it.currentHour = timePreference.hour
            it.currentMinute = timePreference.minute
        }
    }

    override fun onDialogClosed(result: Boolean) {
        if (result) {
            timePicker?.let {
                val timePreference: TimePickerPreference = preference as TimePickerPreference

                timePreference.hour = it.currentHour
                timePreference.minute = it.currentMinute

                timePreference.saveTime()
            }
        }
    }


    override fun findPreference(p0: CharSequence?): Preference {
        return preference
    }
}