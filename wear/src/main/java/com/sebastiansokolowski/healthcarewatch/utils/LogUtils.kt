package com.sebastiansokolowski.healthcarewatch.utils

import android.content.pm.ApplicationInfo
import android.os.Environment
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by Sebastian Sokołowski on 29.10.19.
 */
class LogUtils {
    companion object {
        fun setupLogcat(applicationInfo: ApplicationInfo) {
            if (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)) {
                val file = File(Environment.getExternalStorageDirectory(), "health_care_watch")
                if (!file.exists()) {
                    file.mkdirs()
                }

                val filePath: String = Environment.getExternalStorageDirectory().path +
                        String.format("/health_care_watch/logcat_%s.txt", Calendar.getInstance().time)
                try {
                    Runtime.getRuntime().exec(arrayOf("logcat", "*:D", "-v", "time", "-f", filePath))
                } catch (e: IOException) {
                }
            }
        }
    }
}