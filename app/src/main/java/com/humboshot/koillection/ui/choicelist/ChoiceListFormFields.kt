package com.humboshot.koillection.ui.choicelist

import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.choicelist.ChoiceListWrite

data class ChoiceListFormFields(
    val name: String = "",
    val choices: List<String> = listOf(""),
) {
    /** The choices to send: trimmed, with empty rows left out. */
    val cleanChoices: List<String> get() = choices.map { it.trim() }.filter { it.isNotEmpty() }

    fun validate(): String? {
        val clean = cleanChoices
        return when {
            name.isBlank() -> "Name is required"
            clean.isEmpty() -> "Add at least one choice"
            clean.size != clean.distinct().size -> "Each choice must be different"
            else -> null
        }
    }

    fun toCreate(): ChoiceListWrite = ChoiceListWrite(name = name.trim(), choices = cleanChoices)

    /** Gives only the fields that differ from [original], or null when nothing changed. */
    fun toPatch(original: ChoiceList): ChoiceListWrite? {
        val patch = ChoiceListWrite(
            name = name.trim().takeIf { it != original.name },
            choices = cleanChoices.takeIf { it != original.choices.filterNotNull() },
        )
        return patch.takeIf { it != ChoiceListWrite() }
    }

    fun addChoice(): ChoiceListFormFields = copy(choices = choices + "")

    fun updateChoice(index: Int, value: String): ChoiceListFormFields =
        copy(choices = choices.toMutableList().also { it[index] = value })

    fun removeChoice(index: Int): ChoiceListFormFields =
        copy(choices = choices.toMutableList().also { it.removeAt(index) })

    /** Moves a choice one place up ([delta] = -1) or down ([delta] = 1). Does nothing at the ends. */
    fun moveChoice(index: Int, delta: Int): ChoiceListFormFields {
        val target = index + delta
        if (target !in choices.indices) return this
        return copy(choices = choices.toMutableList().also { it.add(target, it.removeAt(index)) })
    }

    companion object {
        fun from(choiceList: ChoiceList) = ChoiceListFormFields(
            name = choiceList.name,
            choices = choiceList.choices.filterNotNull().ifEmpty { listOf("") },
        )
    }
}
