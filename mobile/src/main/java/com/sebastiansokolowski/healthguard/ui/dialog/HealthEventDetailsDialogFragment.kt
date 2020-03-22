package com.sebastiansokolowski.healthguard.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.sebastiansokolowski.healthguard.R
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity

/**
 * Created by Sebastian Sokołowski on 02.03.20.
 */
class HealthEventDetailsDialogFragment : DialogFragment() {
    companion object {
        private const val MESSAGE_KEY = "message"

        fun newInstance(healthEventEntity: HealthEventEntity): HealthEventDetailsDialogFragment {
            val dialog = HealthEventDetailsDialogFragment()

            val bundle = Bundle()
            bundle.putString(MESSAGE_KEY, healthEventEntity.details + "\n\n\n\n" + healthEventEntity.measurementSettings)
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