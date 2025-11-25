package com.example.fitmatch.data.cloudinary

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.fitmatch.config.CloudinaryConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryRepository {

    /**
     * Sube una imagen a Cloudinary y devuelve la URL segura (secure_url).
     */
    suspend fun uploadImage(uri: Uri): String = suspendCancellableCoroutine { cont ->
        val requestId = MediaManager.get()
            .upload(uri)
            .unsigned(CloudinaryConfig.getUploadPreset())
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) = Unit

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) = Unit

                override fun onSuccess(
                    requestId: String?,
                    resultData: MutableMap<Any?, Any?>?
                ) {
                    val secureUrl = resultData?.get("secure_url") as? String
                    if (secureUrl != null) {
                        cont.resume(secureUrl)
                    } else {
                        cont.resumeWithException(
                            IllegalStateException("Cloudinary no devolvió secure_url")
                        )
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    if (cont.isActive) {
                        cont.resumeWithException(
                            Exception(error?.description ?: "Error subiendo imagen")
                        )
                    }
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    if (cont.isActive) {
                        cont.resumeWithException(
                            Exception(error?.description ?: "Upload reprogramado")
                        )
                    }
                }
            })
            .dispatch() // <-- esto devuelve el requestId (String)

        // Si se cancela la corrutina, cancelamos también el upload en Cloudinary
        cont.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}
