package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.TagService
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory

object TagRepository {
    private val service: TagService
        get() = ApiClient.getInstance().create(TagService::class.java)

    suspend fun getTags(): Result<List<Tag>> =
        getAllPages { page -> service.getTags(page) }
            .map { tags -> tags.sortedBy { it.label.lowercase() } }

    suspend fun getTag(id: String): Result<Tag> = service.getTag(id)

    suspend fun getTagItems(id: String, page: Int): Result<Page<Item>> =
        service.getTagItems(id, page).map { it.toPage() }

    suspend fun getCategories(): Result<List<TagCategory>> =
        getAllPages { page -> service.getCategories(page) }
            .map { categories -> categories.sortedBy { it.label.lowercase() } }

    suspend fun getCategory(id: String): Result<TagCategory> = service.getCategory(id)
}
