package com.sebastiansokolowski.healthguard.view

import android.content.Context
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import android.view.View

/**
 * Created by Sebastian Sokołowski on 28.03.20.
 */
class CustomSnackbar(val context: Context) {
    fun make(view: View, text: String, length: Int): Snackbar {
        val snackbar = Snackbar.make(view, text, length)
        snackbar.setActionTextColor(ContextCompat.getColor(context, android.R.color.white))
        return snackbar
    }
}