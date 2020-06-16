package com.sebastiansokolowski.healthguard.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Sebastian Sokołowski on 06.06.19.
 */
class EntryHelper {
    companion object {
        private val dtf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        private val timestampAtMidnight = initTimestampAtMidnight()

        private fun initTimestampAtMidnight(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar.timeInMillis
        }

        fun getDate(value: Float): String {
            val timestampFromMidnight = value.toInt()
            val date = Date(timestampAtMidnight + timestampFromMidnight)
            return dtf.format(date.time)
        }
    }
}