package com.humboshot.koillection.data.sort

import java.text.Collator
import java.text.SimpleDateFormat
import java.util.Locale

/** How the app sorts collections and items. The API has no sort option, so the app sorts the loaded lists. */
enum class SortOrder(val label: String) {
    TITLE_ASC("Title A–Z"),
    TITLE_DESC("Title Z–A"),
    NEWEST("Newest first"),
    OLDEST("Oldest first");

    fun <T> sort(list: List<T>, title: (T) -> String, createdAt: (T) -> String?): List<T> {
        val byTitle = Comparator<T> { a, b -> NaturalOrder.compare(title(a), title(b)) }
        val byDate = compareBy<T> { parseApiTime(createdAt(it)) ?: Long.MIN_VALUE }.thenComparing(byTitle)
        return when (this) {
            TITLE_ASC -> list.sortedWith(byTitle)
            TITLE_DESC -> list.sortedWith(byTitle.reversed())
            NEWEST -> list.sortedWith(byDate.reversed())
            OLDEST -> list.sortedWith(byDate)
        }
    }
}

/**
 * Compares titles the way people read them: case does not matter, accents follow the phone's language,
 * and numbers compare by value, so "Vol. 2" comes before "Vol. 10".
 */
object NaturalOrder : Comparator<String> {
    private val parts = Regex("\\d+|\\D+")

    override fun compare(a: String, b: String): Int {
        val collator = Collator.getInstance().apply { strength = Collator.SECONDARY }
        val left = parts.findAll(a.trim()).map { it.value }.toList()
        val right = parts.findAll(b.trim()).map { it.value }.toList()
        for (i in 0 until minOf(left.size, right.size)) {
            val x = left[i]
            val y = right[i]
            val result = if (x[0].isDigit() && y[0].isDigit()) compareNumbers(x, y) else collator.compare(x, y)
            if (result != 0) return result
        }
        return left.size.compareTo(right.size)
    }

    private fun compareNumbers(x: String, y: String): Int {
        val a = x.trimStart('0')
        val b = y.trimStart('0')
        return if (a.length != b.length) a.length.compareTo(b.length) else a.compareTo(b)
    }
}

/** Reads an API timestamp such as `2026-10-15T12:30:00+02:00` as epoch milliseconds, or null. */
internal fun parseApiTime(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return runCatching { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(value)?.time }.getOrNull()
}
