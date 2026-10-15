package com.humboshot.koillection.ui.template

import com.humboshot.koillection.data.template.Field
import com.humboshot.koillection.data.template.FieldWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FieldFormFieldsTest {
    private val author = Field(id = "f1", name = "Author", position = 1, type = "text", visibility = "public")
    private val format = Field(
        id = "f2", name = "Format", position = 2, type = "choice-list",
        choiceList = "/api/choice_lists/c1", visibility = "public",
    )

    @Test
    fun validate_needsNameAndChoiceListForChoiceType() {
        assertNotNull(FieldFormFields(name = " ").validate())
        assertNotNull(FieldFormFields(name = "Format", type = "choice-list").validate())
        assertNull(FieldFormFields(name = "Format", type = "choice-list", choiceListId = "c1").validate())
    }

    @Test
    fun toCreate_buildsIris() {
        assertEquals(
            FieldWrite(
                name = "Format", position = 3, type = "choice-list", choiceList = "/api/choice_lists/c1",
                template = "/api/templates/t1", visibility = "public",
            ),
            FieldFormFields(name = "Format ", type = "choice-list", choiceListId = "c1").toCreate("t1", position = 3)
        )
    }

    @Test
    fun toCreate_dropsChoiceListForOtherTypes() {
        assertNull(FieldFormFields(name = "Pages", type = "number", choiceListId = "c1").toCreate("t1", 1).choiceList)
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        assertNull(FieldFormFields.from(format).toPatch(format))
        assertEquals(FieldWrite(name = "Writer"), FieldFormFields.from(author).copy(name = "Writer").toPatch(author))
        assertEquals(
            FieldWrite(choiceList = "/api/choice_lists/c2"),
            FieldFormFields.from(format).copy(choiceListId = "c2").toPatch(format)
        )
    }

    @Test
    fun swapPositions_swapsWithNeighbour() {
        assertEquals(listOf("f2" to 1, "f1" to 2), swapPositions(listOf(author, format), 1, -1))
        assertEquals(emptyList<Pair<String, Int>>(), swapPositions(listOf(author, format), 0, -1))
        assertEquals(emptyList<Pair<String, Int>>(), swapPositions(listOf(author, format), 1, 1))
    }

    @Test
    fun swapPositions_usesIndicesWhenPositionsAreEqual() {
        val same = format.copy(position = 1)

        assertEquals(listOf("f1" to 1, "f2" to 0), swapPositions(listOf(author, same), 0, 1))
    }
}
