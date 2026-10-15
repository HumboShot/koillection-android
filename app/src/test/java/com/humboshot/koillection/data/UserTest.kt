package com.humboshot.koillection.data

import com.humboshot.koillection.data.user.Feature
import com.humboshot.koillection.data.user.User
import com.humboshot.koillection.data.user.currentUser
import com.humboshot.koillection.data.user.enabledFeatures
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserTest {
    private val admin = User(id = "1", username = "admin")
    private val owner = User(id = "2", username = "humboshot")

    @Test
    fun currentUser_picksLoggedInUserFromList() {
        assertEquals(owner, listOf(admin, owner).currentUser("humboshot"))
        assertEquals(owner, listOf(admin, owner).currentUser("HumboShot"))
    }

    @Test
    fun currentUser_isNullWhenMissing() {
        assertNull(listOf(admin).currentUser("humboshot"))
        assertNull(listOf(admin).currentUser(null))
    }

    @Test
    fun enabledFeatures_followsFlags() {
        val user = owner.copy(tagsFeatureEnabled = false, loansFeatureEnabled = false)

        assertEquals(Feature.entries.toSet() - Feature.TAGS - Feature.LOANS, user.enabledFeatures())
    }

    @Test
    fun parsesFlagsFromJson() {
        val json = """{"id":"2","username":"humboshot","email":"x@example.com","wishlistsFeatureEnabled":false}"""

        val user = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(User::class.java).fromJson(json)!!

        assertEquals(false, user.wishlistsFeatureEnabled)
        assertEquals(true, user.tagsFeatureEnabled)
    }
}
