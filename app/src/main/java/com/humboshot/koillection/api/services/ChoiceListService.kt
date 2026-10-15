package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.choicelist.ChoiceListWrite
import com.humboshot.koillection.data.hydra.HydraCollection
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChoiceListService {
    @GET("/api/choice_lists")
    suspend fun getChoiceLists(@Query("page") page: Int = 1): Result<HydraCollection<ChoiceList>>

    @GET("/api/choice_lists/{id}")
    suspend fun getChoiceList(@Path("id") id: String): Result<ChoiceList>

    @POST("/api/choice_lists")
    suspend fun createChoiceList(@Body choiceList: ChoiceListWrite): Result<ChoiceList>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/choice_lists/{id}")
    suspend fun updateChoiceList(@Path("id") id: String, @Body choiceList: ChoiceListWrite): Result<ChoiceList>

    @DELETE("/api/choice_lists/{id}")
    suspend fun deleteChoiceList(@Path("id") id: String): Result<Unit>
}
