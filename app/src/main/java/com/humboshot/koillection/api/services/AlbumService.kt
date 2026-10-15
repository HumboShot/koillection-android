package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.album.Album
import com.humboshot.koillection.data.album.AlbumWrite
import com.humboshot.koillection.data.album.Photo
import com.humboshot.koillection.data.album.PhotoWrite
import com.humboshot.koillection.data.hydra.HydraCollection
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
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

    @POST("/api/albums")
    suspend fun createAlbum(@Body album: AlbumWrite): Result<Album>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/albums/{id}")
    suspend fun updateAlbum(@Path("id") id: String, @Body album: AlbumWrite): Result<Album>

    @DELETE("/api/albums/{id}")
    suspend fun deleteAlbum(@Path("id") id: String): Result<Unit>

    @Multipart
    @POST("/api/albums/{id}/image")
    suspend fun uploadImage(@Path("id") id: String, @Part file: MultipartBody.Part): Result<Album>

    @GET("/api/photos/{id}")
    suspend fun getPhoto(@Path("id") id: String): Result<Photo>

    @POST("/api/photos")
    suspend fun createPhoto(@Body photo: PhotoWrite): Result<Photo>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/photos/{id}")
    suspend fun updatePhoto(@Path("id") id: String, @Body photo: PhotoWrite): Result<Photo>

    @DELETE("/api/photos/{id}")
    suspend fun deletePhoto(@Path("id") id: String): Result<Unit>

    @Multipart
    @POST("/api/photos/{id}/image")
    suspend fun uploadPhotoImage(@Path("id") id: String, @Part file: MultipartBody.Part): Result<Photo>
}
