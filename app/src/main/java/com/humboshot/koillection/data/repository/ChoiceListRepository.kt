package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.ChoiceListService
import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.choicelist.ChoiceListWrite

object ChoiceListRepository {
    private val service: ChoiceListService
        get() = ApiClient.getInstance().create(ChoiceListService::class.java)

    suspend fun getChoiceLists(): Result<List<ChoiceList>> =
        getAllPages { page -> service.getChoiceLists(page) }
            .map { lists -> lists.sortedBy { it.name.lowercase() } }

    suspend fun getChoiceList(id: String): Result<ChoiceList> = service.getChoiceList(id)

    suspend fun createChoiceList(choiceList: ChoiceListWrite): Result<ChoiceList> = service.createChoiceList(choiceList)

    suspend fun updateChoiceList(id: String, choiceList: ChoiceListWrite): Result<ChoiceList> =
        service.updateChoiceList(id, choiceList)

    suspend fun deleteChoiceList(id: String): Result<Unit> = service.deleteChoiceList(id)
}
