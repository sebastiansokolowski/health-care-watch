package com.sebastiansokolowski.healthguard.view.preference

import android.content.Context
import androidx.preference.SeekBarPreference
import android.util.AttributeSet
import com.sebastiansokolowski.healthguard.R

/**
 * Created by Sebastian Sokołowski on 07.07.19.
 */
class CustomSeekBarPreference : SeekBarPreference {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CustomSeekBarPreference,
                0, 0).apply {

            try {
                min = getInteger(R.styleable.CustomSeekBarPreference_myMinValue, 0)
                max = getInteger(R.styleable.CustomSeekBarPreference_myMaxValue, 1)
            } finally {
                recycle()
            }
        }
    }
}