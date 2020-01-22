package com.sebastiansokolowski.healthcarewatch.view.preference

import android.content.Context
import android.support.v7.preference.SeekBarPreference
import android.util.AttributeSet
import com.sebastiansokolowski.shared.dataModel.settings.FallSettings

/**
 * Created by Sebastian Sokołowski on 07.07.19.
 */
class ActivityThresholdSeekBarPreference : SeekBarPreference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        min = 1
        max = 5
        setDefaultValue (FallSettings().activityThreshold)
    }

}