package com.sebastiansokolowski.shared.util

import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Created by Sebastian Sokołowski on 19.01.20.
 */
class Utils {
    companion object {
        fun formatValue(value: Float, maximumFractionDigits: Int = 2): String {
            val df = DecimalFormat()
            df.minimumFractionDigits = 0
            df.maximumFractionDigits = maximumFractionDigits
            val result = df.format(value)
            return if (result.replace(",", ".").toFloatOrNull() == 0.0f) {
                df.negativePrefix = ""
                df.format(value)
            } else {
                result
            }
        }

        fun roundValue(value: Float, scale: Int = 2): Float {
            return (value * 10.toDouble().pow(scale)).roundToInt() /
                    10.toDouble().pow(scale).toFloat()
        }

    }
}