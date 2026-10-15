package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.loan.Loan
import retrofit2.http.GET
import retrofit2.http.Query

interface LoanService {
    @GET("/api/loans")
    suspend fun getLoans(@Query("page") page: Int = 1): Result<HydraCollection<Loan>>
}
