package com.sebastiansokolowski.healthcarewatch.view.preference

import android.content.Context
import android.support.v7.preference.SeekBarPreference
import android.util.AttributeSet
import com.sebastiansokolowski.shared.SettingsSharedPreferences
import com.sebastiansokolowski.shared.dataModel.FallSettings
import com.sebastiansokolowski.shared.dataModel.MeasurementSettings

/**
 * Created by Sebastian Sokołowski on 07.07.19.
 */
class SamplingSeekBarPreference : SeekBarPreference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        min = 100
        max = 9000
        setDefaultValue(MeasurementSettings().samplingUs)
    }


}