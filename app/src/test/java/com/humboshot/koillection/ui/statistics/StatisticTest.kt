package com.humboshot.koillection.ui.statistics

import com.humboshot.koillection.data.hydra.HydraTotal
import com.humboshot.koillection.data.user.Feature
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticTest {
    @Test
    fun unknownFeatures_showEveryTile() {
        assertEquals(Statistic.entries, visibleStatistics(null))
    }

    @Test
    fun disabledFeatures_hideTheirTiles() {
        val features = setOf(Feature.LOANS, Feature.TEMPLATES, Feature.STATISTICS)

        assertEquals(
            listOf(Statistic.COLLECTIONS, Statistic.ITEMS, Statistic.TEMPLATES, Statistic.LOANS, Statistic.OPEN_LOANS),
            visibleStatistics(features)
        )
    }

    @Test
    fun hydraTotal_ignoresMembers() {
        val json = """{"@id":"/api/items","totalItems":42,"member":[{"id":"1","name":"Book"}],"view":{"next":"x"}}"""

        val total = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(HydraTotal::class.java).fromJson(json)

        assertEquals(42, total?.totalItems)
    }
}
