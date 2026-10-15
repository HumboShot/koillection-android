package com.humboshot.koillection.ui.album

import com.humboshot.koillection.data.album.Album
import com.humboshot.koillection.data.album.AlbumWrite
import com.humboshot.koillection.extension.toIri

data class AlbumFormFields(
    val title: String = "",
    val visibility: String = "public",
) {
    fun validate(): String? = if (title.isBlank()) "Title is required" else null

    fun toCreate(parentId: String?): AlbumWrite = AlbumWrite(
        title = title.trim(),
        parent = parentId?.toIri("albums"),
        visibility = visibility,
    )

    /** Gives only the fields that differ from [original], or null when nothing changed. */
    fun toPatch(original: Album): AlbumWrite? {
        val patch = AlbumWrite(
            title = title.trim().takeIf { it != original.title },
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != AlbumWrite() }
    }

    companion object {
        fun from(album: Album) = AlbumFormFields(
            title = album.title,
            visibility = album.visibility ?: "public",
        )
    }
}
