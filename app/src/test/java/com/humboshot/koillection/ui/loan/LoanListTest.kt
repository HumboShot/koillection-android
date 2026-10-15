package com.humboshot.koillection.ui.loan

import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.ui.common.formatApiDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class LoanListTest {
    private val open = Loan(id = "1", item = "/api/items/i1", lentTo = "Anna", lentAt = "2026-10-01T00:00:00+00:00")
    private val returned = open.copy(id = "2", returnedAt = "2026-10-10T00:00:00+00:00")

    @Test
    fun lentFilter_hidesReturnedLoans() {
        assertEquals(listOf(open), filterLoans(listOf(open, returned), showAll = false))
    }

    @Test
    fun allFilter_keepsEveryLoan() {
        assertEquals(listOf(open, returned), filterLoans(listOf(open, returned), showAll = true))
    }

    @Test
    fun formatApiDate_usesDatePart() {
        assertEquals("2026/10/01", formatApiDate("2026-10-01T23:30:00+02:00", SimpleDateFormat("yyyy/MM/dd", Locale.US)))
    }

    @Test
    fun formatApiDate_keepsUnreadableInput() {
        assertEquals("soon", formatApiDate("soon"))
        assertEquals("", formatApiDate(null))
    }
}
