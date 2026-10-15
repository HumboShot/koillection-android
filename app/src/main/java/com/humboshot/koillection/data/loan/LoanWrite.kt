package com.humboshot.koillection.data.loan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@JsonClass(generateAdapter = true)
data class LoanWrite(
    @Json(name = "item")
    val item: String? = null,
    @Json(name = "lentTo")
    val lentTo: String? = null,
    @Json(name = "lentAt")
    val lentAt: String? = null,
    @Json(name = "returnedAt")
    val returnedAt: String? = null,
)

private val utc: TimeZone = TimeZone.getTimeZone("UTC")

/**
 * Turns a date picker value (midnight UTC of the chosen day) into the API date-time format,
 * for example `2026-10-15T00:00:00+00:00`.
 */
fun loanDate(utcMidnightMillis: Long): String =
    SimpleDateFormat("yyyy-MM-dd'T'00:00:00+00:00", Locale.US).apply { timeZone = utc }.format(utcMidnightMillis)

/** Today in the phone's time zone, as midnight UTC. This is the form the date picker uses. */
fun todayUtcMillis(now: Calendar = Calendar.getInstance()): Long =
    Calendar.getInstance(utc).apply {
        clear()
        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
