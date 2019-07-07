package com.selastiansokolowski.healthcarewatch.view.preference

import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.support.v14.preference.MultiSelectListPreference
import android.util.AttributeSet


/**
 * Created by Sebastian Sokołowski on 12.05.19.
 */
class ContactListPreference : MultiSelectListPreference {

    constructor(context: Context) : super(context) {
        setDefaultValues()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setDefaultValues()
    }

    fun setDefaultValues() {
        val myEntries: MutableList<String> = mutableListOf("")
        val myValues: MutableList<String> = mutableListOf("")

        entries = myEntries.toTypedArray()
        entryValues = myValues.toTypedArray()
    }

    fun loadContacts() {
        val contactName = mutableListOf<String>()
        val contactNumber = mutableListOf<String>()

        val contentReceiver = context.contentResolver
        val cursor = contentReceiver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER),
                null,
                null,
                Phone.DISPLAY_NAME + " ASC")

        if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
            do {
                val name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME))
                var number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER))
                number = number.replace(" ", "")
                number = number.replace("-", "")

                if (!contactName.contains(name)) {
                    contactName.add("$name\n\t$number")
                    contactNumber.add(number)
                }
            } while (cursor.moveToNext())

            cursor.close()
        }

        entries = contactName.toTypedArray()
        entryValues = contactNumber.toTypedArray()
    }
}