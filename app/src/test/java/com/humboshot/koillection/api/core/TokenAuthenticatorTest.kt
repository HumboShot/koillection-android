package com.humboshot.koillection.api.core

import com.humboshot.koillection.api.services.LoginService
import com.humboshot.koillection.auth.Session
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TokenAuthenticatorTest {
    private class FakeSession(override var serverUrl: String?) : Session {
        override val username: String = "user"
        override var password: String? = "secret"
        override var token: String? = "old"
        var signedOut = false

        override fun updateToken(token: String) {
            this.token = token
        }

        override fun signOut() {
            signedOut = true
            token = null
        }
    }

    private val server = MockWebServer()
    private lateinit var session: FakeSession
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server.start()
        session = FakeSession(server.url("/").toString())

        val loginClient = OkHttpClient()
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val loginService = { url: String ->
            Retrofit.Builder()
                .baseUrl(url)
                .client(loginClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(ResultCallAdapterFactory())
                .build()
                .create(LoginService::class.java)
        }
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(session))
            .authenticator(TokenAuthenticator(session, loginService))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun get(url: String = server.url("/api/items").toString()) =
        client.newCall(Request.Builder().url(url).build()).execute().use { it.code }

    @Test
    fun unauthorized_withoutSavedPassword_signsOut() {
        session.password = null
        server.enqueue(MockResponse().setResponseCode(401))

        assertEquals(401, get())
        assertTrue(session.signedOut)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun unauthorized_logsInAgainAndRetriesWithNewToken() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("""{"token":"new"}"""))
        server.enqueue(MockResponse().setBody("{}"))

        assertEquals(200, get())

        assertEquals("Bearer old", server.takeRequest().getHeader("Authorization"))
        val login = server.takeRequest()
        assertEquals("/api/authentication_token", login.path)
        assertNull(login.getHeader("Authorization"))
        assertEquals("""{"username":"user","password":"secret"}""", login.body.readUtf8())
        assertEquals("Bearer new", server.takeRequest().getHeader("Authorization"))
        assertEquals("new", session.token)
    }

    @Test
    fun secondUnauthorized_givesUp() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("""{"token":"new"}"""))
        server.enqueue(MockResponse().setResponseCode(401))

        assertEquals(401, get())
        assertEquals(3, server.requestCount)
    }

    @Test
    fun rejectedPassword_signsOut() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))

        assertEquals(401, get())
        assertTrue(session.signedOut)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun serverErrorDuringLogin_keepsSession() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(500))

        assertEquals(401, get())
        assertFalse(session.signedOut)
        assertEquals("old", session.token)
    }

    @Test
    fun networkErrorDuringLogin_keepsSession() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(401, get())
        assertFalse(session.signedOut)
    }

    @Test
    fun loginWithoutToken_failsRequestWithoutCrash() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("""{"message":"ok"}"""))

        assertEquals(401, get())
        assertFalse(session.signedOut)
        assertEquals("old", session.token)
    }

    @Test
    fun failingTokenSave_failsRequestWithoutCrash() {
        val failingSession = object : Session by session {
            override fun updateToken(token: String) = error("Keystore failure")
        }
        val loginService = { url: String ->
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()))
                .build()
                .create(LoginService::class.java)
        }
        val failingClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(failingSession))
            .authenticator(TokenAuthenticator(failingSession, loginService))
            .build()
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("""{"token":"new"}"""))

        val code = failingClient.newCall(Request.Builder().url(server.url("/api/items")).build()).execute().use { it.code }

        assertEquals(401, code)
    }

    @Test
    fun tokenAlreadyRefreshed_retriesWithoutLogin() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("{}"))

        val request = Request.Builder().url(server.url("/api/items")).header("Authorization", "Bearer stale").build()
        val plainClient = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(session) { error("No login expected") })
            .build()

        assertEquals(200, plainClient.newCall(request).execute().use { it.code })
        server.takeRequest()
        assertEquals("Bearer old", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun otherHost_getsNoTokenAndNoLogin() {
        session.serverUrl = "https://collection.example.com/"
        server.enqueue(MockResponse().setResponseCode(401))

        assertEquals(401, get())
        assertNull(server.takeRequest().getHeader("Authorization"))
        assertEquals(1, server.requestCount)
    }
}
