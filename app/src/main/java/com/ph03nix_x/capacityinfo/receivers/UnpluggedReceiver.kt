package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.batteryLevel
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.utils.Utils.capacityAdded
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utils.Utils.isPowerConnected
import com.ph03nix_x.capacityinfo.utils.Utils.percentAdded

class UnpluggedReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(CapacityInfoService.instance != null && isPowerConnected)
            when(intent.action) {

            Intent.ACTION_POWER_DISCONNECTED -> {

                isPowerConnected = false

                pref.edit().apply {
                    
                    if(residualCapacity > 0) {

                        if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                        putInt(RESIDUAL_CAPACITY,
                            (CapacityInfoService.instance!!.getCurrentCapacity(context) * 1000).toInt())
                        else putInt(RESIDUAL_CAPACITY, CapacityInfoService.instance!!.getCurrentCapacity(context).toInt())
                    }

                    if (!CapacityInfoService.instance!!.isFull && CapacityInfoService.instance!!.seconds > 0) {

                        putInt(LAST_CHARGE_TIME, CapacityInfoService.instance!!.seconds)

                        putInt(BATTERY_LEVEL_WITH, CapacityInfoService.instance!!.batteryLevelWith)

                        putInt(BATTERY_LEVEL_TO, CapacityInfoService.instance!!.getBatteryLevel(context))

                        if(capacityAdded > 0) putFloat(CAPACITY_ADDED, capacityAdded.toFloat())

                        if(percentAdded > 0) putInt(PERCENT_ADDED, percentAdded)

                        percentAdded = 0

                        capacityAdded = 0.0
                    }
                    
                    apply()
                }

                CapacityInfoService.instance!!.seconds = 0

                batteryLevel = 0

                CapacityInfoService.instance!!.isFull = false
            }
        }
    }
}