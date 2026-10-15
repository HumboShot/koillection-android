package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.album.Album
import com.humboshot.koillection.data.album.Photo
import com.humboshot.koillection.data.hydra.HydraCollection
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AlbumService {
    @GET("/api/albums")
    suspend fun getAlbums(@Query("page") page: Int = 1): Result<HydraCollection<Album>>

    @GET("/api/albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Result<Album>

    @GET("/api/albums/{id}/children")
    suspend fun getChildren(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Album>>

    @GET("/api/albums/{id}/photos")
    suspend fun getPhotos(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Photo>>
}
