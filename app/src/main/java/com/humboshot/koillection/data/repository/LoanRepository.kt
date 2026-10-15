package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.LoanService
import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.data.loan.LoanWrite
import com.humboshot.koillection.data.loan.loanDate
import com.humboshot.koillection.extension.toIri

object LoanRepository {
    private val service: LoanService
        get() = ApiClient.getInstance().create(LoanService::class.java)

    /** `/api/loans` has no filter or order parameters, so the app sorts: newest loan first. */
    suspend fun getLoans(): Result<List<Loan>> =
        getAllPages { page -> service.getLoans(page) }
            .map { loans -> loans.sortedByDescending { it.lentAt } }

    suspend fun getItemLoans(itemId: String): Result<List<Loan>> =
        getAllPages { page -> service.getItemLoans(itemId, page) }
            .map { loans -> loans.sortedByDescending { it.lentAt } }

    suspend fun lend(itemId: String, lentTo: String, lentAtMillis: Long): Result<Loan> =
        service.createLoan(LoanWrite(item = itemId.toIri("items"), lentTo = lentTo, lentAt = loanDate(lentAtMillis)))

    suspend fun markReturned(loanId: String, returnedAtMillis: Long): Result<Loan> =
        service.updateLoan(loanId, LoanWrite(returnedAt = loanDate(returnedAtMillis)))

    suspend fun deleteLoan(loanId: String): Result<Unit> = service.deleteLoan(loanId)
}
