package com.example.fitmatch.config

import android.content.Context
import com.cloudinary.android.MediaManager
import com.example.fitmatch.R

object CloudinaryConfig {
    private var cloudName: String = ""
    private var uploadPreset: String = ""
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        cloudName = context.getString(R.string.cloudinary_cloud_name)
        uploadPreset = context.getString(R.string.cloudinary_upload_preset)

        val config = mapOf(
            "cloud_name" to cloudName
        )

        MediaManager.init(context, config)
        initialized=true;
    }
    fun getCloudName() = cloudName
    fun getUploadPreset() = uploadPreset
}