package com.ph03nix_x.capacityinfo.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.android.billingclient.api.BillingClient
import com.ph03nix_x.capacityinfo.utils.Constants.GOOGLE_PLAY_PACKAGE_NAME

object Utils {

    var billingClient: BillingClient? = null
    var isPowerConnected = false
    var isInstalledGooglePlay = true
    var isDonated = false
    var tempCurrentCapacity = 0.0
    var capacityAdded = 0.0
    var tempBatteryLevelWith = 0
    var percentAdded = 0
    var batteryIntent: Intent? = null

    fun launchActivity(context: Context, activity: Class<*>) {

        context.startActivity(Intent(context, activity))
    }

    fun launchActivity(context: Context, activity: Class<*>, flags: ArrayList<Int>) {

        context.startActivity(Intent(context, activity).apply {

            flags.forEach {

                this.addFlags(it)
            }
        })
    }

    fun launchActivity(context: Context, activity: Class<*>, intent: Intent) {

        context.startActivity(Intent(context, activity).apply {

            putExtras(intent)
        })
    }

    fun launchActivity(context: Context, activity: Class<*>, flags: ArrayList<Int>, intent: Intent) {

        context.startActivity(Intent(context, activity).apply {

            flags.forEach {

                this.addFlags(it)
            }
            putExtras(intent)
        })
    }

    fun isGooglePlay(context: Context) =
        GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallerPackageName(context.packageName)

    fun isInstalledGooglePlay(context: Context): Boolean {

        return try {

            context.packageManager.getPackageInfo(GOOGLE_PLAY_PACKAGE_NAME, 0)

            true
        }

        catch (e: PackageManager.NameNotFoundException) { false }
    }
}