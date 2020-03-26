package com.sebastiansokolowski.shared.util

import java.text.DecimalFormat

/**
 * Created by Sebastian Sokołowski on 19.01.20.
 */
class Utils {
    companion object {
        fun format(float: Float, digits: Int): String {
            val df = DecimalFormat()
            df.maximumFractionDigits = digits
            return df.format(float)
        }

    }
}