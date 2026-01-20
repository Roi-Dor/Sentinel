package com.example.sentinel

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.example.sentinel_sdk.SecurityReport
import java.io.File
import java.util.UUID

object SecurityScanner {

    fun scan(context: Context): SecurityReport {
        var score = 100

        // ---------------------------------------------------
        // CHECK 1: Root Access (Updated for your Emulator)
        // ---------------------------------------------------
        val isRooted = checkRootMethod1() || checkRootMethod2()
        if (isRooted) score -= 50 // almost no device should be rooted

        // ---------------------------------------------------
        // CHECK 2: USB Debugging
        // ---------------------------------------------------
        val adbSetting = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
        val isUsbDebugging = (adbSetting == 1)
        if (isUsbDebugging) score -= 15

        // ---------------------------------------------------
        // CHECK 3: Emulator Detection (Updated for "sdk_gphone")
        // ---------------------------------------------------
        val isEmulator = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("google/sdk_gphone") // <--- NEW CHECK
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("sdk_gphone") // <--- NEW CHECK
                || Build.MANUFACTURER.contains("Genymotion"))
        if (isEmulator) score -= 30

        // ---------------------------------------------------
        // CHECK 4: Sideload Detection
        // ---------------------------------------------------
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        val isSideloaded = installer != "com.android.vending"
        if (isSideloaded) score -= 0 //for now its 0 but in the future it should be 20

        // ---------------------------------------------------
        // CHECK 5: Physical USB Connection (Data Port)
        // ---------------------------------------------------
        val batteryIntent = context.registerReceiver(null,
            android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val isUsbConnected = plugged == android.os.BatteryManager.BATTERY_PLUGGED_USB
        if (isUsbConnected) score -= 15


        if (score < 0) score = 0

        return SecurityReport(
            device_id = UUID.randomUUID().toString(),
            app_id = context.packageName,
            score = score,
            is_rooted = isRooted,
            is_usb_debugging = isUsbDebugging,
            is_emulator = isEmulator,
            is_sideloaded = isSideloaded,
            is_usb_connected = isUsbConnected
        )
    }

    // Helper: Check for test-keys OR dev-keys
    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && (buildTags.contains("test-keys") || buildTags.contains("dev-keys"))
    }

    // Helper: Check for binary files (Standard check)
    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }
}