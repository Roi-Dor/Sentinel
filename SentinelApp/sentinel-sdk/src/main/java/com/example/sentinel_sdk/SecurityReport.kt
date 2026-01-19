package com.example.sentinel_sdk

data class SecurityReport(
    val device_id: String,
    val app_id: String,
    val score: Int,
    val is_rooted: Boolean,
    val is_usb_debugging: Boolean,
    val is_emulator: Boolean,
    val is_sideloaded: Boolean
)

