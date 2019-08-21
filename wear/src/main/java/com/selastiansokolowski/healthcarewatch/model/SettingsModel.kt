package com.selastiansokolowski.healthcarewatch.model

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import com.google.android.gms.wearable.*
import com.selastiansokolowski.healthcarewatch.utils.HealthCareEnginesUtils
import com.selastiansokolowski.shared.DataClientPaths
import com.selastiansokolowski.shared.SettingsSharedPreferences
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 06.07.19.
 */
class SettingsModel(context: Context, private val sensorManager: SensorManager, private val sharedPreferences: SharedPreferences) : DataClient.OnDataChangedListener {
    private val TAG = javaClass.canonicalName

    var sensorDataModel: SensorDataModel? = null

    init {
        Wearable.getDataClient(context).addListener(this)
    }

    fun getSamplingUs(): Int {
        return sharedPreferences.getInt(SettingsSharedPreferences.SAMPLING_US,
                TimeUnit.SECONDS.toMicros(SettingsSharedPreferences.SAMPLING_US_DEFAULT.toLong()).toInt())
    }

    fun getSensors(): List<Int> {
        val sensors = sharedPreferences.getStringSet(SettingsSharedPreferences.SENSORS,
                getDefaultSensors())

        return sensors?.map { sensor -> sensor.toInt() } ?: emptyList()
    }

    private fun getDefaultSensors(): Set<String> {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val supportedHealthCareEngines = HealthCareEnginesUtils.getSupportedHealthCareEngines(sensors)
        return supportedHealthCareEngines.flatMap { it.requiredSensors() }.map { it.toString() }.toSet()
    }

    override fun onDataChanged(dataEvent: DataEventBuffer) {
        dataEvent.forEach { event ->
            Log.d(TAG, "onDataChanged path:${event.dataItem.uri.path}")
            if (event.type != DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "type not changed")
                return
            }
            when (event.dataItem.uri.path) {
                DataClientPaths.SETTINGS_MAP_PATH -> {
                    DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                        val samplingUs = getInt(DataClientPaths.SETTINGS_MAP_SAMPLING_US)

                        val healthCareEvents = getStringArrayList(DataClientPaths.SETTINGS_MAP_HEALTH_CARE_EVENTS)
                        val healthCareEngines = HealthCareEnginesUtils.getHealthCareEngines(healthCareEvents)
                        val sensors = healthCareEngines.flatMap { it.requiredSensors() }.map { it.toString() }.toSet()

                        sharedPreferences.edit().apply {
                            putInt(SettingsSharedPreferences.SAMPLING_US, samplingUs)
                            putStringSet(SettingsSharedPreferences.SENSORS, sensors)
                            commit()
                        }

                        Log.d(TAG, "settings samplingUs=$samplingUs healthCareEvents=$healthCareEvents")
                        sensorDataModel?.refreshSettings()
                    }
                }
            }
        }
    }
}