package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import android.content.SharedPreferences
import android.provider.ContactsContract
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.model.SetupModel
import com.selastiansokolowski.healthcarewatch.view.preference.CustomMultiSelectListPreference
import com.selastiansokolowski.shared.SettingsSharedPreferences
import com.selastiansokolowski.shared.healthCare.HealthCareEventType
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SettingsViewModel
@Inject constructor(private val sharedPreferences: SharedPreferences, private val wearableDataClient: WearableDataClient, private val contentResolver: ContentResolver, val setupModel: SetupModel) : ViewModel() {
    fun onSharedPreferenceChanged(key: String) {
        when (key) {
            SettingsSharedPreferences.SAMPLING_US -> {
                updatedSettings()
            }
        }
    }

    private fun updatedSettings() {
        val refreshRate = sharedPreferences.getInt(SettingsSharedPreferences.SAMPLING_US, SettingsSharedPreferences.SAMPLING_US_DEFAULT)

        val sampleUs = TimeUnit.SECONDS.toMicros(refreshRate.toLong()).toInt()
        val healthCareEvents = sharedPreferences.getStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()

        val settings = WearableDataClient.Settings(sampleUs, healthCareEvents)
        wearableDataClient.sendSettings(settings)
    }

    fun setupPreference(preference: CustomMultiSelectListPreference) {
        when (preference.key) {
            SettingsSharedPreferences.CONTACTS -> {
                setContacts(preference)
            }
            SettingsSharedPreferences.HEALTH_CARE_EVENTS -> {
                setSupportedHealthCareEvents(preference)
            }
        }
    }

    private fun setContacts(preference: CustomMultiSelectListPreference) {
        val names = mutableListOf<String>()
        val values = mutableListOf<String>()

        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")

        if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
            do {
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                number = number.replace(" ", "")
                number = number.replace("-", "")

                if (!names.contains(name)) {
                    names.add("$name\n\t$number")
                    values.add(number)
                }
            } while (cursor.moveToNext())

            cursor.close()
        }

        preference.setValues(names.toTypedArray(), values.toTypedArray())
    }

    private fun setSupportedHealthCareEvents(preference: CustomMultiSelectListPreference) {
        val names = mutableListOf<String>()
        val values = mutableListOf<String>()

        val healthCareEventsName = sharedPreferences
                .getStringSet(SettingsSharedPreferences.SUPPORTED_HEALTH_CARE_EVENTS, emptySet())
                ?: emptySet()
        val healthCareEvents = healthCareEventsName.mapNotNull {
            try {
                HealthCareEventType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        healthCareEvents.forEach {
            names.add(it.title)
            values.add(it.name)
        }

        preference.setValues(names.toTypedArray(), values.toTypedArray())
    }

}