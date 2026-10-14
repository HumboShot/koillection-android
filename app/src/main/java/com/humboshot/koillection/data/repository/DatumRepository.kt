package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.DatumService
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.datum.DatumWrite

object DatumRepository {
    private val service: DatumService
        get() = ApiClient.getInstance().create(DatumService::class.java)

    suspend fun createDatum(datum: DatumWrite): Result<Datum> = service.createDatum(datum)

    suspend fun updateDatum(id: String, datum: DatumWrite): Result<Datum> = service.updateDatum(id, datum)

    suspend fun deleteDatum(id: String): Result<Unit> = service.deleteDatum(id)
}
