package com.sebastiansokolowski.healthcarewatch.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.sebastiansokolowski.healthcarewatch.R
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity

/**
 * Created by Sebastian Sokołowski on 02.03.20.
 */
class HealthCareEventDetailsDialogFragment : DialogFragment() {
    companion object {
        private const val MESSAGE_KEY = "message"

        fun newInstance(healthCareEventEntity: HealthCareEventEntity): HealthCareEventDetailsDialogFragment {
            val dialog = HealthCareEventDetailsDialogFragment()

            val bundle = Bundle()
            bundle.putString(MESSAGE_KEY, healthCareEventEntity.details + "\n\n\n\n" + healthCareEventEntity.measurementSettings)
            dialog.arguments = bundle

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var message: String = arguments?.getString(MESSAGE_KEY) ?: "empty"
        message = message.replace(" ", "")
        message = message.replace(",", ",\n")
        message = message.replace("{", "{\n")
        message = message.replace("(", "(\n")
        message = message.replace("[", "[\n")

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                    .setPositiveButton(R.string.action_ok) { dialog, id ->
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}