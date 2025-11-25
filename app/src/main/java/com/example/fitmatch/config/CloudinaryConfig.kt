package com.example.fitmatch.config

import android.content.Context
import com.cloudinary.android.MediaManager
import com.example.fitmatch.R

object CloudinaryConfig {
    private var cloudName: String = ""
    private var uploadPreset: String = ""
    fun initialize(context: Context) {
        cloudName = context.getString(R.string.cloudinary_cloud_name)
        uploadPreset = context.getString(R.string.cloudinary_upload_preset)

        val config = mapOf(
            "cloud_name" to cloudName
        )

        MediaManager.init(context, config)
    }
    fun getCloudName() = cloudName
    fun getUploadPreset() = uploadPreset
}