package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.ItemService
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.tag.Tag

object ItemRepository {
    private val service: ItemService
        get() = ApiClient.getInstance().create(ItemService::class.java)

    suspend fun getItem(id: String): Result<Item> = service.getItem(id)

    suspend fun getData(id: String): Result<List<Datum>> =
        getAllPages { page -> service.getData(id, page) }
            .map { data -> data.sortedBy { it.position ?: Int.MAX_VALUE } }

    suspend fun getTags(id: String): Result<List<Tag>> =
        getAllPages { page -> service.getTags(id, page) }

    suspend fun getRelatedItems(id: String): Result<List<Item>> =
        getAllPages { page -> service.getRelatedItems(id, page) }
}
