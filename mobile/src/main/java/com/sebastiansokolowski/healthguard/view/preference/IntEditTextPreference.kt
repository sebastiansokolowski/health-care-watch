package com.sebastiansokolowski.healthguard.view.preference

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference


/**
 * Created by Sebastian Sokołowski on 12.05.19.
 */
class IntEditTextPreference : EditTextPreference {

    constructor(context: Context) : super(context) {
        setInputType()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setInputType()
    }

    private fun setInputType() {
        setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedInt(-1).toString()
    }

    override fun persistString(value: String?): Boolean {
        return if (value != null) {
            persistInt(Integer.valueOf(value))
        } else {
            super.persistString(value)
        }
    }
}