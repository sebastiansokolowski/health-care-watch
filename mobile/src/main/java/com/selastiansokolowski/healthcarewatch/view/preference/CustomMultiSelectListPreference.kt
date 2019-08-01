package com.selastiansokolowski.healthcarewatch.view.preference

import android.content.Context
import android.support.v14.preference.MultiSelectListPreference
import android.util.AttributeSet


/**
 * Created by Sebastian Sokołowski on 12.05.19.
 */
class CustomMultiSelectListPreference : MultiSelectListPreference {

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

    fun setValues(entryName: Array<String>, entryValue: Array<String>) {
        entries = entryName
        entryValues = entryValue
    }

}