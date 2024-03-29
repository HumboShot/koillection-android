package com.humboshot.koillection

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.humboshot.koillection.models.UserCredentials

class UserContext {
    var baseAddress: String = getSharedPreferences().getString(MainApplication.applicationContext.getString(R.string.usercontext_domain), "") ?: ""
    var jwt: String = getSharedPreferences().getString(MainApplication.applicationContext.getString(R.string.usercontext_jwt), "") ?: ""

    fun getCredentials(): UserCredentials {
        val sharedPreferences = getSharedPreferences()
        val username = sharedPreferences.getString(MainApplication.applicationContext.getString(R.string.usercontext_username), "") ?: ""
        val password = sharedPreferences.getString(MainApplication.applicationContext.getString(R.string.usercontext_password), "") ?: ""
        val domain = sharedPreferences.getString(MainApplication.applicationContext.getString(R.string.usercontext_domain), "") ?: ""

        return UserCredentials(username, password, domain)
    }

    fun updateJWT(jwt: String) {
        with(getSharedPreferences().edit()) {
            putString(MainApplication.applicationContext.getString(R.string.usercontext_jwt), jwt)
            apply()
        }
    }

    fun setDomain(domain: String) {
        with(getSharedPreferences().edit()) {
            putString(MainApplication.applicationContext.getString(R.string.usercontext_domain), domain)
            apply()
        }
    }

    fun setUser(username: String, password: String, domain: String, jwt: String) {
        with(getSharedPreferences().edit()) {
            putString(MainApplication.applicationContext.getString(R.string.usercontext_username), username)
            putString(MainApplication.applicationContext.getString(R.string.usercontext_password), password)
            putString(MainApplication.applicationContext.getString(R.string.usercontext_domain), domain)
            putString(MainApplication.applicationContext.getString(R.string.usercontext_jwt), jwt)
            apply()
        }
    }

    fun removeUserFromStorage() {
        with(getSharedPreferences().edit()) {
            remove(MainApplication.applicationContext.getString(R.string.usercontext_username))
            remove(MainApplication.applicationContext.getString(R.string.usercontext_password))
            remove(MainApplication.applicationContext.getString(R.string.usercontext_domain))
            remove(MainApplication.applicationContext.getString(R.string.usercontext_jwt))
            apply()
        }
    }

    private fun getSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(MainApplication.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            MainApplication.applicationContext,
            "koillection_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        val instance: UserContext by lazy { UserContext() }
    }
}
