package com.humboshot.koillection.data.repository

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Builds the `file` part that the `/api/{items|collections}/{id}/image` endpoints expect. */
fun imagePart(bytes: ByteArray, mimeType: String?): MultipartBody.Part {
    val extension = mimeType?.substringAfter('/', "")?.ifEmpty { null } ?: "jpg"
    return MultipartBody.Part.createFormData(
        "file",
        "upload.$extension",
        bytes.toRequestBody(mimeType?.toMediaTypeOrNull())
    )
}

/** Reads a picked image. Call it off the main thread. */
fun readImagePart(contentResolver: ContentResolver, uri: Uri): MultipartBody.Part {
    val bytes = checkNotNull(contentResolver.openInputStream(uri)) { "Cannot open the image" }.use { it.readBytes() }
    return imagePart(bytes, contentResolver.getType(uri))
}
