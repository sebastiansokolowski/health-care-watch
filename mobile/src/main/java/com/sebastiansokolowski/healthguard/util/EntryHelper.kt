package com.sebastiansokolowski.healthguard.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class EntryHelper {
    companion object {
        private val dtf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        private val dtfMilliseconds = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

        private val timestampAtMidnight = initTimestampAtMidnight()

        private fun initTimestampAtMidnight(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar.timeInMillis
        }

        fun getDate(value: Float, showMilliseconds: Boolean = false): String {
            val timestampFromMidnight = value.toInt()
            val date = Date(timestampAtMidnight + timestampFromMidnight)
            return if (showMilliseconds) {
                dtfMilliseconds.format(date.time)
            } else {
                dtf.format(date.time)
            }
        }
    }
}