package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.datum.DatumWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatumDraftsTest {
    private fun datum(id: String, type: String = "text", label: String = "Label $id", value: String = "Value $id", position: Int? = null) = Datum(
        id = id,
        type = type,
        label = label,
        value = value,
        position = position,
        currency = null,
        image = null,
        imageSmallThumbnail = null,
        imageLargeThumbnail = null,
        file = null,
        video = null,
        originalFilename = null
    )

    private fun draft(key: Int, original: Datum) = DatumDraft(key, original, DatumFormFields.from(original))

    @Test
    fun datumChanges_isEmptyWhenNothingChanged() {
        val originals = listOf(datum("a"), datum("b"))

        assertTrue(datumChanges(originals, originals.mapIndexed(::draft)).isEmpty)
    }

    @Test
    fun datumChanges_findsDeletesPatchesAndCreates() {
        val a = datum("a")
        val b = datum("b")
        val changedB = draft(1, b).let { it.copy(fields = it.fields.copy(value = "New")) }
        val new = DatumDraft(2, null, DatumFormFields(label = "Pages", type = "number", value = "12"))

        val changes = datumChanges(listOf(a, b), listOf(changedB, new))

        assertEquals(listOf(a), changes.deletes)
        assertEquals(listOf(b to DatumWrite(value = "New")), changes.patches)
        assertEquals(listOf(new), changes.creates)
    }

    @Test
    fun datumChanges_neverDeletesTypesTheFormCannotEdit() {
        val image = datum("img", type = "image")

        assertTrue(datumChanges(listOf(image), emptyList()).isEmpty)
    }

    @Test
    fun nextPosition_followsTheHighestPosition() {
        assertEquals(1, emptyList<Datum>().nextPosition())
        assertEquals(6, listOf(datum("a", position = 2), datum("b", position = 5), datum("c")).nextPosition())
    }
}
