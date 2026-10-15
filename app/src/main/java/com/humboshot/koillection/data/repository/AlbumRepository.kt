package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.AlbumService
import com.humboshot.koillection.data.album.Album
import com.humboshot.koillection.data.album.Photo

object AlbumRepository {
    private val service: AlbumService
        get() = ApiClient.getInstance().create(AlbumService::class.java)

    /** `/api/albums` returns every album, so keep only the ones without a parent. */
    suspend fun getRootAlbums(): Result<List<Album>> =
        getAllPages { page -> service.getAlbums(page) }
            .map { albums -> albums.filter { it.parent == null }.sortedBy { it.title.lowercase() } }

    suspend fun getAlbum(id: String): Result<Album> = service.getAlbum(id)

    suspend fun getChildren(id: String): Result<List<Album>> =
        getAllPages { page -> service.getChildren(id, page) }
            .map { albums -> albums.sortedBy { it.title.lowercase() } }

    /** Photos sorted by the date they were taken, oldest first. Photos without a date come last. */
    suspend fun getPhotos(id: String): Result<List<Photo>> =
        getAllPages { page -> service.getPhotos(id, page) }
            .map { photos -> sortPhotos(photos) }
}

fun sortPhotos(photos: List<Photo>): List<Photo> =
    photos.sortedWith(compareBy<Photo> { it.takenAt == null }.thenBy { it.takenAt }.thenBy { it.title.lowercase() })
