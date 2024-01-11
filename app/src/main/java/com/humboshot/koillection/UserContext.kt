package com.humboshot.koillection

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class UserContext {
    var jwt: String = getJWT()

    private fun getJWT(): String {
        return getSharedPreferences().getString(MainApplication.applicationContext.getString(R.string.usercontext_jwt), "") ?: ""
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
