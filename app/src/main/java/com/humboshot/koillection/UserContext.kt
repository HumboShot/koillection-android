package com.humboshot.koillection

import android.content.SharedPreferences
import androidx.core.content.edit
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
        getSharedPreferences().edit {
            putString(MainApplication.applicationContext.getString(R.string.usercontext_jwt), jwt)
        }
    }

    fun setDomain(domain: String) {
        getSharedPreferences().edit {
            putString(MainApplication.applicationContext.getString(R.string.usercontext_domain), domain)
        }
    }

    fun setUser(username: String, password: String, domain: String, jwt: String) {
        getSharedPreferences().edit {
            putString(MainApplication.applicationContext.getString(R.string.usercontext_username), username)
            putString(MainApplication.applicationContext.getString(R.string.usercontext_password), password)
            putString(MainApplication.applicationContext.getString(R.string.usercontext_domain), domain)
            putString(MainApplication.applicationContext.getString(R.string.usercontext_jwt), jwt)
        }

    }

    fun removeUserFromStorage() {
        getSharedPreferences().edit {
            remove(MainApplication.applicationContext.getString(R.string.usercontext_username))
            remove(MainApplication.applicationContext.getString(R.string.usercontext_password))
            remove(MainApplication.applicationContext.getString(R.string.usercontext_domain))
            remove(MainApplication.applicationContext.getString(R.string.usercontext_jwt))
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
