package com.humboshot.koillection.api

import com.humboshot.koillection.api.core.ResultCallAdapterFactory
import com.humboshot.koillection.api.services.CollectionService
import com.humboshot.koillection.api.services.ItemService
import com.humboshot.koillection.data.collection.CollectionWrite
import com.humboshot.koillection.data.item.ItemWrite
import com.humboshot.koillection.data.repository.imagePart
import com.humboshot.koillection.extension.toIri
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** Uses the same converter and call adapter as `ApiClient` against a local fake server. */
class WriteRequestsTest {
    private val server = MockWebServer()
    private lateinit var items: ItemService
    private lateinit var collections: CollectionService

    private val itemJson = """
        {"id":"i1","name":"Book","quantity":1,"collection":"/api/collections/c1","image":null,
         "imageSmallThumbnail":null,"imageLargeThumbnail":null,"visibility":"public",
         "createdAt":"2026-01-01T00:00:00+00:00","updatedAt":null}
    """.trimIndent()

    @Before
    fun setUp() {
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
        items = retrofit.create(ItemService::class.java)
        collections = retrofit.create(CollectionService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun delete_noContentIsSuccess() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val result = items.deleteItem("i1")

        assertEquals(Result.success(Unit), result)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun patch_usesMergePatchAndSkipsNulls() = runTest {
        server.enqueue(MockResponse().setBody(itemJson))

        items.updateItem("i1", ItemWrite(name = "Book"))

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/items/i1", request.path)
        assertTrue(request.getHeader("Content-Type").orEmpty().startsWith("application/merge-patch+json"))
        assertEquals("""{"name":"Book"}""", request.body.readUtf8())
    }

    @Test
    fun post_sendsIri() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(itemJson))

        val result = items.createItem(ItemWrite(name = "Book", collection = "c1".toIri("collections")))

        assertTrue(result.isSuccess)
        val body = server.takeRequest().body.readUtf8()
        assertEquals("""{"name":"Book","collection":"/api/collections/c1"}""", body)
    }

    @Test
    fun post_collectionSendsParentIri() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        collections.createCollection(CollectionWrite(title = "Child", parent = "p1".toIri("collections")))

        assertEquals("""{"title":"Child","parent":"/api/collections/p1"}""", server.takeRequest().body.readUtf8())
    }

    @Test
    fun imageUpload_sendsFilePart() = runTest {
        server.enqueue(MockResponse().setBody(itemJson))

        items.uploadImage("i1", imagePart(byteArrayOf(1, 2, 3), "image/png"))

        val request = server.takeRequest()
        assertEquals("/api/items/i1/image", request.path)
        assertTrue(request.getHeader("Content-Type").orEmpty().startsWith("multipart/form-data"))
        assertTrue(request.body.readUtf8().contains("name=\"file\"; filename=\"upload.png\""))
    }
}
