package com.humboshot.koillection.data

import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.hydra.HydraView
import com.humboshot.koillection.data.repository.getAllPages
import com.humboshot.koillection.data.repository.toPage
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HydraPagingTest {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter<HydraCollection<KCollection>>(
        Types.newParameterizedType(HydraCollection::class.java, KCollection::class.java)
    )

    private val json = """
        {
          "@context": "/api/contexts/Collection",
          "@id": "/api/collections",
          "@type": "Collection",
          "totalItems": 31,
          "member": [
            {
              "@id": "/api/collections/1",
              "@type": "Collection",
              "id": "1",
              "title": "Root",
              "parent": null,
              "owner": "/api/users/u",
              "color": "#ffffff",
              "image": "uploads/root.png",
              "seenCounter": 3,
              "itemsDefaultTemplate": null,
              "visibility": "public",
              "parentVisibility": null,
              "finalVisibility": "public",
              "scrapedFromUrl": null,
              "createdAt": "2024-01-01T00:00:00+00:00",
              "updatedAt": null
            },
            {
              "id": "2",
              "title": "Child",
              "parent": "/api/collections/1",
              "owner": null,
              "color": "#000000",
              "image": null,
              "seenCounter": 0,
              "itemsDefaultTemplate": null,
              "visibility": "private",
              "parentVisibility": "public",
              "finalVisibility": "private",
              "scrapedFromUrl": null,
              "createdAt": "2024-01-02T00:00:00+00:00",
              "updatedAt": null
            }
          ],
          "view": {
            "@id": "/api/collections?page=1",
            "@type": "PartialCollectionView",
            "first": "/api/collections?page=1",
            "last": "/api/collections?page=2",
            "next": "/api/collections?page=2"
          }
        }
    """.trimIndent()

    @Test
    fun parsesMembersAndNextLink() {
        val result = adapter.fromJson(json)!!

        assertEquals(31, result.totalItems)
        assertEquals(listOf("Root", "Child"), result.member.map { it.title })
        assertNull(result.member[0].parent)
        assertEquals("/api/collections/1", result.member[1].parent)
        assertEquals("/api/collections?page=2", result.view?.next)
        assertTrue(result.toPage().hasNext)
    }

    @Test
    fun parsesLastPageWithoutView() {
        val result = adapter.fromJson("""{"member": [], "totalItems": 0}""")!!

        assertNull(result.view)
        assertFalse(result.toPage().hasNext)
    }

    @Test
    fun getAllPages_followsNextUntilNull() = runTest {
        val requested = mutableListOf<Int>()
        val result = getAllPages { page ->
            requested += page
            val next = if (page < 3) "/api/x?page=${page + 1}" else null
            Result.success(HydraCollection(listOf("p$page"), 3, HydraView(next)))
        }

        assertEquals(listOf(1, 2, 3), requested)
        assertEquals(listOf("p1", "p2", "p3"), result.getOrThrow())
    }

    @Test
    fun getAllPages_failsWhenAPageFails() = runTest {
        val result = getAllPages<String> { page ->
            if (page == 2) Result.failure(RuntimeException("boom"))
            else Result.success(HydraCollection(listOf("p$page"), null, HydraView("/next")))
        }

        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }
}
