package com.sebastiansokolowski.shared.util

import java.text.DecimalFormat

/**
 * Created by Sebastian Sokołowski on 19.01.20.
 */
class Utils {
    companion object {
        fun format(float: Float, maximumFractionDigits: Int): String {
            val df = DecimalFormat()
            df.minimumFractionDigits = 0
            df.maximumFractionDigits = maximumFractionDigits
            val result = df.format(float)
            return if (result.replace(",", ".").toFloatOrNull() == 0.0f) {
                df.negativePrefix = ""
                df.format(float)
            } else {
                result
            }
        }

    }
}