package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.activity.SettingsActivity
import java.lang.Exception

interface DebugOptionsInterface {

    fun changeSettingDialog(context: Context, pref: SharedPreferences) {

        val prefKeysArray = mutableListOf<String>()

        Preferences.values().forEach {

            prefKeysArray.add(it.prefKey)
        }

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_pref_key, null)

        dialog.setView(view)

        val changePrefKey = view.findViewById<EditText>(R.id.change_pref_key_edit)

        val changePrefValue = view.findViewById<EditText>(R.id.change_pref_value_edit)

        val spinner = view.findViewById<Spinner>(R.id.type_spinner).apply {

            isEnabled = false

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                    changePrefValue.setText("")

                    when(position) {

                        0, 1 -> changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")

                        2 -> changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")

                        3 -> changePrefValue.keyListener = DigitsKeyListener.getInstance("01")

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) { }
            }
        }

        var key = ""
        var value: Any = ""

        changePrefKey.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                key = s.toString()
                changePrefValue.isEnabled = key in prefKeysArray
                spinner.isEnabled = key in prefKeysArray

                if(key in prefKeysArray)
                when(key) {

                    Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                    Preferences.ChargeCounter.prefKey, Preferences.PercentAdded.prefKey -> spinner.setSelection(0)

                    Preferences.CapacityAdded.prefKey -> spinner.setSelection(2)

                    Preferences.NumberOfCharges.prefKey -> spinner.setSelection(1)

                    else -> spinner.setSelection(3)
                }

                else changePrefValue.setText("")
            }
        })

        changePrefValue.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { value = s.toString() }
        })

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            if(key != "")
                try {

                    if(key in prefKeysArray) {

                        var isInt = false
                        var isLong = false
                        var isFloat = false
                        var isBoolean = false

                        when(key) {

                            Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                            Preferences.ChargeCounter.prefKey, Preferences.PercentAdded.prefKey -> isInt = true

                            Preferences.CapacityAdded.prefKey -> isFloat = true

                            Preferences.NumberOfCharges.prefKey -> isLong = true

                            else -> isBoolean = true
                        }

                        when (spinner.selectedItemId) {

                            0.toLong() -> changeKey(context, pref, key, value.toString().toInt(), isInt)
                            1.toLong() -> changeKey(context, pref, key, value.toString().toLong(), isLong)
                            2.toLong() -> changeKey(context, pref, key, value.toString().toFloat(), isFloat)
                            3.toLong() -> {

                                if(value == "1") value = true else if(value == "0") value = false

                                changeKey(context, pref, key, value.toString().toBoolean(), isBoolean)
                            }
                        }
                    }

                    else Toast.makeText(context, context.getString(R.string.key_not_found, key), Toast.LENGTH_LONG).show()
                }

                catch (e: Exception) { Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show() }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Int, isInt: Boolean) {

        if(isInt) {

            pref.edit().putInt(key, value).apply()

            Toast.makeText(context, context.getString(R.string.success_change_key, key), Toast.LENGTH_LONG).show()

            if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) MainActivity.instance?.recreate()

            (context as SettingsActivity).recreate()
        }

        else Toast.makeText(context, context.getString(R.string.error_changing_key, key), Toast.LENGTH_LONG).show()

    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Long, isLong: Boolean) {

        if(isLong) {

            pref.edit().putLong(key, value).apply()

            Toast.makeText(context, context.getString(R.string.success_change_key, key), Toast.LENGTH_LONG).show()

            if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) MainActivity.instance?.recreate()

            (context as SettingsActivity).recreate()
        }

        else Toast.makeText(context, context.getString(R.string.error_changing_key, key), Toast.LENGTH_LONG).show()
    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Float, isFloat: Boolean) {

        if(isFloat) {

            pref.edit().putFloat(key, value).apply()

            Toast.makeText(context, context.getString(R.string.success_change_key, key), Toast.LENGTH_LONG).show()

            if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) MainActivity.instance?.recreate()

            (context as SettingsActivity).recreate()
        }

        else Toast.makeText(context, context.getString(R.string.error_changing_key, key), Toast.LENGTH_LONG).show()
    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Boolean, isBoolean: Boolean) {

        if(isBoolean) {

            pref.edit().putBoolean(key, value).apply()

            Toast.makeText(context, context.getString(R.string.success_change_key, key), Toast.LENGTH_LONG).show()

            if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) MainActivity.instance?.recreate()

            (context as SettingsActivity).recreate()
        }

        else Toast.makeText(context, context.getString(R.string.error_changing_key, key), Toast.LENGTH_LONG).show()
    }

    fun resetSettingDialog(context: Context, pref: SharedPreferences) {

        val prefKeysArray = mutableListOf<String>()

        Preferences.values().forEach {

            prefKeysArray.add(it.prefKey)
        }

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.reset_pref_key, null)

        var key = ""

        dialog.setView(view)

        val resetPrefKey = view.findViewById<EditText>(R.id.reset_pref_key_edit)

        resetPrefKey.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { key = s.toString() }
        })

        dialog.setPositiveButton(context.getString(R.string.reset)) { _, _ ->

            if(key != "")
                if(key in prefKeysArray) {

                    pref.edit().remove(key).apply()

                    if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) MainActivity.instance?.recreate()

                    (context as SettingsActivity).recreate()

                } else Toast.makeText(context, context.getString(R.string.key_not_found, key), Toast.LENGTH_LONG).show()

        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()

    }

    fun resetSettingsDialog(context: Context, pref: SharedPreferences) {

        MaterialAlertDialogBuilder(context).apply {

            setTitle(context.getString(R.string.reset_settings))
            setMessage(context.getString(R.string.are_you_sure))
            setPositiveButton(context.getString(R.string.reset)) { _, _ ->

                pref.edit().clear().apply()
                MainActivity.instance?.recreate()
                (context as SettingsActivity).recreate()

                Toast.makeText(context, context.getString(R.string.settings_reset_successfully), Toast.LENGTH_LONG).show()
            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }
}