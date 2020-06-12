package com.sebastiansokolowski.healthguard.viewModel

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import com.sebastiansokolowski.healthguard.model.SettingsModel
import com.sebastiansokolowski.healthguard.model.SetupModel
import com.sebastiansokolowski.healthguard.util.HealthEventHelper
import com.sebastiansokolowski.healthguard.util.SingleEvent
import com.sebastiansokolowski.healthguard.view.preference.CustomMultiSelectListPreference
import com.sebastiansokolowski.shared.SettingsSharedPreferences
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class SettingsViewModel
@Inject constructor(context: Context, private val settingsModel: SettingsModel, private val measurementModel: MeasurementModel, private val contentResolver: ContentResolver, val setupModel: SetupModel) : ViewModel() {

    private val healthEventHelper = HealthEventHelper(context)

    val refreshView: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()
    val showSelectContactDialog: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()

    fun onSharedPreferenceChanged(key: String) {
        when (key) {
            SettingsSharedPreferences.HEALTH_EVENTS -> {
                if (measurementModel.measurementRunning.get()) {
                    measurementModel.stopMeasurement()
                    measurementModel.requestStartMeasurement()
                }
            }
        }
    }

    fun setupPreference(preference: CustomMultiSelectListPreference) {
        when (preference.key) {
            SettingsSharedPreferences.CONTACTS -> {
                setContacts(preference)
            }
            SettingsSharedPreferences.HEALTH_EVENTS -> {
                setSupportedHealthEvents(preference)
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

    private fun setSupportedHealthEvents(preference: CustomMultiSelectListPreference) {
        val names = mutableListOf<String>()
        val values = mutableListOf<String>()

        val healthEventTypesName = settingsModel.getSupportedHealthEventTypes()

        healthEventTypesName.forEach {
            names.add(healthEventHelper.getTitle(it))
            values.add(it.name)
        }

        preference.setValues(names.toTypedArray(), values.toTypedArray())
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (permissions.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return
        }
        when (permissions[0]) {
            Manifest.permission.READ_CONTACTS -> {
                showSelectContactDialog.postValue(SingleEvent(true))
            }
            Manifest.permission.SEND_SMS -> {
                settingsModel.saveSetting(SettingsSharedPreferences.SMS_NOTIFICATIONS, true)
                refreshView.postValue(SingleEvent(true))
            }
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                settingsModel.saveSetting(SettingsSharedPreferences.SMS_USER_LOCATION, true)
                refreshView.postValue(SingleEvent(true))
            }
        }
    }
}