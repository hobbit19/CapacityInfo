package com.ph03nix_x.capacityinfo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.StopService
import java.lang.RuntimeException
import java.text.DecimalFormat

@SuppressWarnings("StaticFieldLeak")
interface NotificationInterface : BatteryInfoInterface {

    companion object {

        private lateinit var notificationBuilder: NotificationCompat.Builder
        private lateinit var channelId: String
        private const val notificationId = 101
    }

    fun createNotification(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(context) else ""
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        val openApp = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        val stopService = PendingIntent.getService(context, 1, Intent(context, StopService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder = NotificationCompat.Builder(context, channelId).apply {

            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.service_small_icon)
            color = ContextCompat.getColor(context.applicationContext, R.color.blue)
            setContentIntent(openApp)

            when(plugged) {

                BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                    if(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true))
                        setStyle(NotificationCompat.BigTextStyle().bigText(getNotificationMessage(context)))

                    else setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.enabled)))
                }

                else -> {

                    if(pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true))
                        setStyle(NotificationCompat.BigTextStyle().bigText(getNotificationMessage(context)))

                    else setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.enabled)))
                }
            }

            setShowWhen(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true)
                    && pref.getBoolean(Preferences.IsServiceHours.prefKey, false))

            if(pref.getBoolean(Preferences.IsShowServiceStop.prefKey, true))
                addAction(NotificationCompat.Action(0, context.getString(R.string.stop_service), stopService))
        }

        if(pref.getBoolean(Preferences.IsEnableService.prefKey, true))
        (context as CapacityInfoService).startForeground(notificationId, notificationBuilder.build())
        else (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)

    }

    fun updateNotification(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val stopService = PendingIntent.getService(context, 1, Intent(context, StopService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.apply {

            when(plugged) {

                BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                    if(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true))
                        setStyle(NotificationCompat.BigTextStyle().bigText(getNotificationMessage(context)))

                    else setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.enabled)))
                }

                else -> {

                    if(pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true))
                        setStyle(NotificationCompat.BigTextStyle().bigText(getNotificationMessage(context)))

                    else setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.enabled)))
                }
            }

            setShowWhen(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true)
                    && pref.getBoolean(Preferences.IsServiceHours.prefKey, false))

            if(pref.getBoolean(Preferences.IsShowServiceStop.prefKey, true) && mActions.isEmpty())
                addAction(NotificationCompat.Action(0, context.getString(R.string.stop_service), stopService))

            else if(!pref.getBoolean(Preferences.IsShowServiceStop.prefKey, true) && mActions.isNotEmpty()) mActions.clear()
        }

        if(pref.getBoolean(Preferences.IsEnableService.prefKey, true))
        notificationManager.notify(notificationId, notificationBuilder.build())
        else notificationManager.cancel(notificationId)
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context): String {
        val channelId = "service_channel"
        val channelName = context.getString(R.string.service)
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_LOW)
        chan.setShowBadge(false)
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun getNotificationMessage(context: Context): String {

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        return when(batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> getBatteryStatusCharging(context)

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> getBatteryStatusNotCharging(context)

            BatteryManager.BATTERY_STATUS_FULL -> getBatteryStatusFull(context)

            BatteryManager.BATTERY_STATUS_DISCHARGING -> getBatteryStatusDischarging(context)

            BatteryManager.BATTERY_STATUS_UNKNOWN -> getBatteryStatusUnknown(context)

            else -> "N/A"
        }
    }

    private fun getBatteryStatusCharging(context: Context): String {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val charging = context.getString(R.string.status, context.getString(R.string.charging))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val plugged = getPlugged(context, batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)!!)
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val chargingCurrent = context.getString(R.string.charging_current, getChargingCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(getCurrentCapacity(context) > 0)
            if(pref.getBoolean(Preferences.IsShowCapacityAddedInNotification.prefKey, true))
                "$charging\n$batteryLevel\n$plugged\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$currentCapacity\n$capacityAdded\n$chargingCurrent\n$temperature\n$voltage"
            else "$charging\n$batteryLevel\n$plugged\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$currentCapacity\n$chargingCurrent\n$temperature\n$voltage"

        else "$charging\n$batteryLevel\n$plugged\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$chargingCurrent\n$temperature\n$voltage"
    }

    private fun getBatteryStatusNotCharging(context: Context): String {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val notCharging = context.getString(R.string.status, context.getString(R.string.not_charging))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val dischargingCurrent = context.getString(R.string.discharge_current, getChargingCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))
        return if(getCurrentCapacity(context) > 0)
            if(pref.getBoolean(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey, true))
                "$notCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$currentCapacity\n$capacityAdded\n$dischargingCurrent\n$temperature\n$voltage"
            else "$notCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$currentCapacity\n$dischargingCurrent\n$temperature\n$voltage"

        else "$notCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$dischargingCurrent\n$temperature\n$voltage"
    }

    private fun getBatteryStatusFull(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val fullCharging = context.getString(R.string.status, context.getString(R.string.full))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val dischargingCurrent = context.getString(R.string.discharge_current, getChargingCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(pref.getBoolean(Preferences.IsSupported.prefKey, true)) {

            if(getCurrentCapacity(context) > 0)
                if(pref.getBoolean(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey, true))
                    "$fullCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$currentCapacity\n$capacityAdded\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargingCurrent\n$temperature\n$voltage"
                else "$fullCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$currentCapacity\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargingCurrent\n$temperature\n$voltage"

            else "$fullCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargingCurrent\n$temperature\n$voltage"
        }

        else "$fullCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds.toDouble())}\n$dischargingCurrent\n$temperature"
    }

    private fun getBatteryStatusDischarging(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryLevelWith = "${pref.getInt(Preferences.BatteryLevelWith.prefKey, 0)}%"
        val batteryLevelTo = "${pref.getInt(Preferences.BatteryLevelTo.prefKey, 0)}%"

        val discharging = context.getString(R.string.status, context.getString(R.string.discharging))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val lastChargingTime = context.getString(R.string.last_charge_time, getLastChargeTime(context), batteryLevelWith, batteryLevelTo)
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val dischargingCurrent = context.getString(R.string.discharge_current, getChargingCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(getCurrentCapacity(context) > 0) {

            if(pref.getInt(Preferences.LastChargeTime.prefKey, 0) > 0 && pref.getBoolean(Preferences.IsShowLastChargeTimeInNotification.prefKey, true))

                if(pref.getBoolean(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey, true))
                    "$discharging\n$batteryLevel\n$lastChargingTime\n$currentCapacity\n$capacityAdded\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargingCurrent\n$temperature\n$voltage"
                else "$discharging\n$batteryLevel\n$lastChargingTime\n$currentCapacity\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargingCurrent\n$temperature\n$voltage"

            else {

                if(pref.getBoolean(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey, true))
                    "$discharging\n$batteryLevel\n$currentCapacity\n$capacityAdded\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargingCurrent\n$temperature\n$voltage"
                else "$discharging\n$batteryLevel\n$currentCapacity\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargingCurrent\n$temperature\n$voltage"
            }
        }

        else {

            if(pref.getInt(Preferences.LastChargeTime.prefKey, 0) > 0 && pref.getBoolean(Preferences.IsShowLastChargeTimeInNotification.prefKey, true))
                "$discharging\n$batteryLevel\n$lastChargingTime\n$dischargingCurrent\n$temperature\n$voltage"

            else "$discharging\n$dischargingCurrent\n$temperature\n$voltage"
        }
    }

    private fun getBatteryStatusUnknown(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val discharging = context.getString(R.string.status, context.getString(R.string.unknown))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val temperature = context.getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(getCurrentCapacity(context) > 0)
            if(pref.getBoolean(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey, true))
                "$discharging\n$batteryLevel\n$currentCapacity\n$capacityAdded\n$temperature\n$voltage"
            else "$discharging\n$batteryLevel\n$currentCapacity\n$temperature\n$voltage"

        else "$discharging\n$batteryLevel\n$temperature\n$voltage"
    }
}