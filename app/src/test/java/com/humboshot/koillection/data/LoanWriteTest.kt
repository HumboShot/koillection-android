package com.humboshot.koillection.data

import com.humboshot.koillection.data.loan.LoanWrite
import com.humboshot.koillection.data.loan.loanDate
import com.humboshot.koillection.data.loan.todayUtcMillis
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class LoanWriteTest {
    @Test
    fun loanDate_formatsUtcMidnight() {
        // 2026-10-15T00:00:00Z
        assertEquals("2026-10-15T00:00:00+00:00", loanDate(1_792_022_400_000L))
    }

    @Test
    fun todayUtcMillis_keepsLocalDate() {
        // Late evening in a zone ahead of UTC must still give the local day.
        val lateInTokyo = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 15, 23, 30)
        }

        assertEquals("2026-10-15T00:00:00+00:00", loanDate(todayUtcMillis(lateInTokyo)))
    }

    @Test
    fun returnPatch_sendsOnlyReturnedAt() {
        val json = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            .adapter(LoanWrite::class.java)
            .toJson(LoanWrite(returnedAt = "2026-10-15T00:00:00+00:00"))

        assertEquals("""{"returnedAt":"2026-10-15T00:00:00+00:00"}""", json)
    }
}
