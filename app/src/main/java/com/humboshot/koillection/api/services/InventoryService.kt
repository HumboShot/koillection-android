package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.inventory.Inventory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryService {
    @GET("/api/inventories")
    suspend fun getInventories(@Query("page") page: Int = 1): Result<HydraCollection<Inventory>>

    @GET("/api/inventories/{id}")
    suspend fun getInventory(@Path("id") id: String): Result<Inventory>

    @DELETE("/api/inventories/{id}")
    suspend fun deleteInventory(@Path("id") id: String): Result<Unit>
}
