package com.humboshot.koillection.data.sort

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SortOrderTest {
    private data class Entry(val title: String, val createdAt: String?)

    private val entries = listOf(
        Entry("vol. 10", "2026-01-03T10:00:00+00:00"),
        Entry("Banana", "2026-01-01T10:00:00+00:00"),
        Entry("Vol. 2", "2026-01-02T10:00:00+02:00"),
        Entry("apple", null),
    )

    private fun titles(order: SortOrder) = order.sort(entries, { it.title }, { it.createdAt }).map { it.title }

    @Test
    fun titleAscending_ignoresCaseAndComparesNumbersByValue() {
        assertEquals(listOf("apple", "Banana", "Vol. 2", "vol. 10"), titles(SortOrder.TITLE_ASC))
    }

    @Test
    fun titleDescending_isTheReverse() {
        assertEquals(listOf("vol. 10", "Vol. 2", "Banana", "apple"), titles(SortOrder.TITLE_DESC))
    }

    @Test
    fun newestFirst_usesTheTimeAndPutsUnknownDatesLast() {
        assertEquals(listOf("vol. 10", "Vol. 2", "Banana", "apple"), titles(SortOrder.NEWEST))
    }

    @Test
    fun oldestFirst_putsUnknownDatesFirst() {
        assertEquals(listOf("apple", "Banana", "Vol. 2", "vol. 10"), titles(SortOrder.OLDEST))
    }

    @Test
    fun naturalOrder_handlesLeadingZerosAndPrefixes() {
        assertEquals(listOf("Item", "Item 007", "Item 8", "Item 10"), listOf("Item 10", "Item 8", "Item", "Item 007").sortedWith(NaturalOrder))
    }

    @Test
    fun parseApiTime_readsTheOffset() {
        assertEquals(0L, parseApiTime("1970-01-01T02:00:00+02:00"))
        assertNull(parseApiTime("not a date"))
    }
}
