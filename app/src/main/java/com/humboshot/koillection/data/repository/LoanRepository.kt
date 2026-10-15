package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.LoanService
import com.humboshot.koillection.data.loan.Loan

object LoanRepository {
    private val service: LoanService
        get() = ApiClient.getInstance().create(LoanService::class.java)

    /** `/api/loans` has no filter or order parameters, so the app sorts: newest loan first. */
    suspend fun getLoans(): Result<List<Loan>> =
        getAllPages { page -> service.getLoans(page) }
            .map { loans -> loans.sortedByDescending { it.lentAt } }
}
