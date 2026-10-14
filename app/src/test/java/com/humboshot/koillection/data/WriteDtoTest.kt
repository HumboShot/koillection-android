package com.humboshot.koillection.data

import com.humboshot.koillection.data.collection.CollectionWrite
import com.humboshot.koillection.data.datum.DatumWrite
import com.humboshot.koillection.data.item.ItemWrite
import com.humboshot.koillection.extension.toIri
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class WriteDtoTest {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Test
    fun itemWrite_sendsCollectionIriAndSkipsNulls() {
        val json = moshi.adapter(ItemWrite::class.java)
            .toJson(ItemWrite(name = "Book", quantity = 2, collection = "abc".toIri("collections")))

        assertEquals("""{"name":"Book","quantity":2,"collection":"/api/collections/abc"}""", json)
    }

    @Test
    fun itemWrite_patchWithOneField() {
        val json = moshi.adapter(ItemWrite::class.java).toJson(ItemWrite(name = "New name"))

        assertEquals("""{"name":"New name"}""", json)
    }

    @Test
    fun collectionWrite_sendsParentIriAndSkipsNulls() {
        val json = moshi.adapter(CollectionWrite::class.java)
            .toJson(CollectionWrite(title = "Books", parent = "p1".toIri("collections"), visibility = "public"))

        assertEquals("""{"title":"Books","parent":"/api/collections/p1","visibility":"public"}""", json)
    }

    @Test
    fun datumWrite_sendsItemIriAndSkipsNulls() {
        val json = moshi.adapter(DatumWrite::class.java).toJson(
            DatumWrite(item = "i1".toIri("items"), type = "text", label = "Author", value = "Tolkien", position = 1)
        )

        assertEquals(
            """{"item":"/api/items/i1","type":"text","label":"Author","value":"Tolkien","position":1}""",
            json
        )
    }
}
