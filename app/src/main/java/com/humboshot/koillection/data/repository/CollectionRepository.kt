package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.CollectionService
import com.humboshot.koillection.data.collection.CollectionWrite
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.item.Item
import okhttp3.MultipartBody

object CollectionRepository {
    private val service: CollectionService
        get() = ApiClient.getInstance().create(CollectionService::class.java)

    /** `/api/collections` returns every collection, so keep only the ones without a parent. */
    suspend fun getRootCollections(): Result<List<KCollection>> =
        getAllPages { page -> service.getCollections(page) }
            .map { collections -> collections.filter { it.parent == null } }

    suspend fun getCollection(id: String): Result<KCollection> = service.getCollection(id)

    suspend fun getChildren(id: String): Result<List<KCollection>> =
        getAllPages { page -> service.getChildren(id, page) }

    suspend fun getData(id: String): Result<List<Datum>> =
        getAllPages { page -> service.getData(id, page) }
            .map { data -> data.sortedBy { it.position ?: Int.MAX_VALUE } }

    suspend fun getItems(id: String, page: Int): Result<Page<Item>> =
        service.getItems(id, page).map { it.toPage() }

    suspend fun createCollection(collection: CollectionWrite): Result<KCollection> =
        service.createCollection(collection)

    suspend fun updateCollection(id: String, collection: CollectionWrite): Result<KCollection> =
        service.updateCollection(id, collection)

    suspend fun deleteCollection(id: String): Result<Unit> = service.deleteCollection(id)

    suspend fun uploadImage(id: String, file: MultipartBody.Part): Result<KCollection> = service.uploadImage(id, file)
}
