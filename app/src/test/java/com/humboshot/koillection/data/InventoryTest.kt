package com.humboshot.koillection.data

import com.humboshot.koillection.data.inventory.Inventory
import com.humboshot.koillection.data.inventory.InventoryItem
import com.humboshot.koillection.data.inventory.parseInventory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryTest {
    private val adapter = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(Inventory::class.java)

    @Test
    fun parsesNestedSectionsAndCheckedItems() {
        val json = """
            {"id":"i1","name":"Check 2026","content":[
              {"title":"Books","items":[{"name":"A","checked":true},{"name":"B","checked":false}],
               "children":[{"title":"Manga","items":[{"name":"C","checked":1}],"children":[]}]}
            ]}
        """.trimIndent()

        val sections = parseInventory(adapter.fromJson(json)!!.content)

        assertEquals("Books", sections.single().title)
        assertEquals(listOf(InventoryItem("A", true), InventoryItem("B", false)), sections.single().items)
        assertEquals(listOf(InventoryItem("C", true)), sections.single().children.single().items)
        assertEquals(3, sections.single().totalItems)
        assertEquals(2, sections.single().checkedItems)
    }

    @Test
    fun keepsStringsAndSkipsUnknownValues() {
        val json = """{"id":"i1","name":"Old","content":["Books",42,null]}"""

        val sections = parseInventory(adapter.fromJson(json)!!.content)

        assertEquals(listOf("Books"), sections.map { it.title })
    }
}
