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
        // CHECK 1: Root Access (The heavy hitter)
        // ---------------------------------------------------
        val isRooted = checkRootMethod1() || checkRootMethod2()
        if (isRooted) score -= 40

        // ---------------------------------------------------
        // CHECK 2: USB Debugging (The physical risk)
        // ---------------------------------------------------
        val adbSetting = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
        val isUsbDebugging = (adbSetting == 1)
        if (isUsbDebugging) score -= 20

        // ---------------------------------------------------
        // CHECK 3: Emulator Detection (The bot risk)
        // ---------------------------------------------------
        val isEmulator = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion"))
        if (isEmulator) score -= 20

        // ---------------------------------------------------
        // CHECK 4: Sideload Detection (The tampering risk)
        // ---------------------------------------------------
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        val isSideloaded = installer != "com.android.vending"


        if (isSideloaded) score -= 20


        if (score < 0) score = 0

        return SecurityReport(
            device_id = UUID.randomUUID().toString(),
            app_id = context.packageName,
            score = score,
            is_rooted = isRooted,
            is_usb_debugging = isUsbDebugging,
            is_emulator = isEmulator,
            is_sideloaded = isSideloaded
        )
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }


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