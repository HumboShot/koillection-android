package com.humboshot.koillection.ui.common

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Shows the date part of an API timestamp such as `2026-10-15T00:00:00+00:00` in the phone's medium
 * date style. Returns the input when it cannot be read.
 */
fun formatApiDate(value: String?, format: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        format.format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value.take(10))!!)
    }.getOrDefault(value)
}
