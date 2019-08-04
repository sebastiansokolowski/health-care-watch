package com.selastiansokolowski.healthcarewatch.model

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.wearable.*
import com.selastiansokolowski.shared.DataClientPaths
import com.selastiansokolowski.shared.SettingsSharedPreferences
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 06.07.19.
 */
class SettingsModel(context: Context, private val sharedPreferences: SharedPreferences) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    private val sensorsDefault = listOf(
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION
    )

    var sensorDataModel: SensorDataModel? = null

    init {
        Wearable.getDataClient(context).addListener(this)
    }

    fun getSamplingUs(): Int {
        return sharedPreferences.getInt(SettingsSharedPreferences.SAMPLING_US,
                TimeUnit.SECONDS.toMicros(SettingsSharedPreferences.SAMPLING_US_DEFAULT.toLong()).toInt())
    }

    fun getSensors(): List<Int> {
        val sensors = sharedPreferences.getStringSet(SettingsSharedPreferences.SENSORS, null)

        return if (sensors != null) {
            val result = mutableListOf<Int>()

            sensors.forEach {
                val value = it.toInt()
                result.add(value)
            }

            result
        } else {
            sensorsDefault
        }
    }

    override fun onDataChanged(dataEvent: DataEventBuffer) {
        dataEvent.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "type not changed")
                return
            }
            when (event.dataItem.uri.path) {
                DataClientPaths.SETTINGS_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val samplingUs = getInt(DataClientPaths.SETTINGS_MAP_SAMPLING_US)
                        val sensors = getIntegerArrayList(DataClientPaths.SETTINGS_MAP_SENSORS)

                        sharedPreferences.edit().apply {
                            putInt(SettingsSharedPreferences.SAMPLING_US, samplingUs)
                            putIntegerArrayList(SettingsSharedPreferences.SENSORS, sensors)
                            commit()
                        }

                        sensorDataModel?.refreshSettings()
                    }
                }
            }
        }
    }
}