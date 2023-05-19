package com.example.flashlight

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class CustomSpinnerAdapter(
    context: Context,
    resource: Int,
    objects: Array<String>,
    private val textColors: IntArray
) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        val textColor = if (isDarkTheme()) {
            ContextCompat.getColor(context, android.R.color.white) // Boja teksta za tamnu temu
        } else {
            ContextCompat.getColor(context, android.R.color.black) // Boja teksta za svijetlu temu
        }
        textView.setTextColor(textColor)
        val itemBackgroundColor = if (isDarkTheme()) {
            ContextCompat.getColor(context, R.color.dark_background) // Pozadina za tamnu temu
        } else {
            ContextCompat.getColor(context, R.color.light_background) // Pozadina za svijetlu temu
        }
        view.setBackgroundColor(itemBackgroundColor)
        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        val textColor = if (isDarkTheme()) {
            ContextCompat.getColor(context, android.R.color.white) // Boja teksta za tamnu temu
        } else {
            ContextCompat.getColor(context, android.R.color.black) // Boja teksta za svijetlu temu
        }
        textView.setTextColor(textColor)
        val itemBackgroundColor = if (isDarkTheme()) {
            ContextCompat.getColor(context, R.color.dark_background) // Pozadina za tamnu temu
        } else {
            ContextCompat.getColor(context, R.color.light_background) // Pozadina za svijetlu temu
        }
        view.setBackgroundColor(itemBackgroundColor)

        return view
    }

    private fun isDarkTheme(): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}