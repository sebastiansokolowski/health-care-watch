package com.selastiansokolowski.healthcarewatch.view

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.preference.DialogPreference
import android.util.AttributeSet

/**
 * Created by Sebastian Sokołowski on 26.04.19.
 */
class TimePickerPreference : DialogPreference {

    var hour = 0
    var minute = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun loadTime(time: String) {
        try {
            val array = time.split(":")
            hour = array[0].toInt()
            minute = array[1].toInt()
        } catch (e: Exception) {
        }
    }

    fun saveTime() {
        val time = String.format("%02d", hour) + ":" + String.format("%02d", minute)
        summary = time
        if (callChangeListener(time)) {
            persistString(time)
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getString(index) ?: Any()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val time = getPersistedString(defaultValue?.toString() ?: "00:00")
        loadTime(time)
        summary = time
    }

}