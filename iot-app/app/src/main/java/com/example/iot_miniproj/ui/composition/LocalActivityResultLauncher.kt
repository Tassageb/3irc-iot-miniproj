package com.example.iot_miniproj.ui.composition

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.compositionLocalOf

val LocalActivityResultLauncher = compositionLocalOf<ActivityResultLauncher<Intent>> {
    error("No ActivityResultLauncher provided")
}