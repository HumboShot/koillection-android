package com.humboshot.koillection.data

import com.humboshot.koillection.data.log.Log
import com.humboshot.koillection.data.log.filterLogs
import org.junit.Assert.assertEquals
import org.junit.Test

class LogTest {
    private fun log(id: String, type: String?, at: String) = Log(
        id = id, type = type, loggedAt = at, objectId = "o$id", objectLabel = "Label $id", objectClass = "App\\Entity\\Item",
    )

    private val created = log("1", "create", "2026-01-01T10:00:00+00:00")
    private val deleted = log("2", "delete", "2026-02-01T10:00:00+00:00")

    @Test
    fun shortClass_keepsLastPart() {
        assertEquals("Item", created.shortClass)
        assertEquals("Collection", created.copy(objectClass = "Collection").shortClass)
    }

    @Test
    fun filterLogs_sortsNewestFirst() {
        assertEquals(listOf(deleted, created), filterLogs(listOf(created, deleted), null))
    }

    @Test
    fun filterLogs_keepsOneType() {
        assertEquals(listOf(created), filterLogs(listOf(created, deleted), "create"))
    }
}
