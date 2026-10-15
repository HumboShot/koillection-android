package com.humboshot.koillection.ui.choicelist

import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.choicelist.ChoiceListWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ChoiceListFormFieldsTest {
    private val original = ChoiceList(id = "c1", name = "Condition", choices = listOf("New", "Used"))

    @Test
    fun validate_needsNameAndOneChoice() {
        assertNotNull(ChoiceListFormFields(name = " ", choices = listOf("New")).validate())
        assertNotNull(ChoiceListFormFields(name = "Condition", choices = listOf(" ", "")).validate())
        assertNull(ChoiceListFormFields(name = "Condition", choices = listOf("New", "")).validate())
    }

    @Test
    fun validate_rejectsDuplicates() {
        assertNotNull(ChoiceListFormFields(name = "Condition", choices = listOf("New", " New")).validate())
    }

    @Test
    fun toCreate_trimsAndDropsEmptyRows() {
        assertEquals(
            ChoiceListWrite(name = "Condition", choices = listOf("New", "Used")),
            ChoiceListFormFields(name = "Condition ", choices = listOf(" New", "", "Used")).toCreate()
        )
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        val fields = ChoiceListFormFields.from(original)

        assertNull(fields.toPatch(original))
        assertEquals(ChoiceListWrite(choices = listOf("Used", "New")), fields.moveChoice(0, 1).toPatch(original))
        assertEquals(ChoiceListWrite(name = "State"), fields.copy(name = "State").toPatch(original))
    }

    @Test
    fun moveChoice_staysInsideList() {
        val fields = ChoiceListFormFields.from(original)

        assertEquals(fields, fields.moveChoice(0, -1))
        assertEquals(fields, fields.moveChoice(1, 1))
        assertEquals(listOf("Used", "New"), fields.moveChoice(1, -1).choices)
    }

    @Test
    fun addAndRemoveChoice() {
        val fields = ChoiceListFormFields.from(original).addChoice().updateChoice(2, "Broken").removeChoice(0)

        assertEquals(listOf("Used", "Broken"), fields.choices)
    }
}
