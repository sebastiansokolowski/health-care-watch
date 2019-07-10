package com.selastiansokolowski.healthcarewatch.view.preference

import android.content.Context
import android.support.v14.preference.MultiSelectListPreference
import android.util.AttributeSet
import com.selastiansokolowski.shared.db.entity.HealthCareEventType


/**
 * Created by Sebastian Sokołowski on 12.05.19.
 */
class HealthCareEnginesListPreference : MultiSelectListPreference {

    constructor(context: Context) : super(context) {
        setDefaultValues()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setDefaultValues()
    }

    private fun setDefaultValues() {
        val myEntries: MutableList<String> = mutableListOf("")
        val myValues: MutableList<String> = mutableListOf("")

        entries = myEntries.toTypedArray()
        entryValues = myValues.toTypedArray()
    }

    fun loadEngines() {
        val engineName = mutableListOf<String>()
        val engineKey = mutableListOf<String>()

        HealthCareEventType.values().forEach {
            engineName.add(it.title)
            engineKey.add(it.name)
        }

        entries = engineName.toTypedArray()
        entryValues = engineKey.toTypedArray()
    }
}