package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.data.loan.LoanWrite
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LoanService {
    @GET("/api/loans")
    suspend fun getLoans(@Query("page") page: Int = 1): Result<HydraCollection<Loan>>

    @GET("/api/items/{id}/loans")
    suspend fun getItemLoans(@Path("id") itemId: String, @Query("page") page: Int = 1): Result<HydraCollection<Loan>>

    @POST("/api/loans")
    suspend fun createLoan(@Body loan: LoanWrite): Result<Loan>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/loans/{id}")
    suspend fun updateLoan(@Path("id") id: String, @Body loan: LoanWrite): Result<Loan>

    @DELETE("/api/loans/{id}")
    suspend fun deleteLoan(@Path("id") id: String): Result<Unit>
}
