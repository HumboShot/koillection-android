package com.humboshot.koillection.api.core

import okhttp3.Request
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultCallTest {
    private class FakeCall<T>(private val response: Response<T>) : Call<T> {
        override fun execute(): Response<T> = response
        override fun enqueue(callback: Callback<T>) = callback.onResponse(this, response)
        override fun isExecuted(): Boolean = true
        override fun cancel() = Unit
        override fun isCanceled(): Boolean = false
        override fun clone(): Call<T> = FakeCall(response)
        override fun request(): Request = Request.Builder().url("https://example.com/").build()
        override fun timeout(): Timeout = Timeout.NONE
    }

    private fun <T> enqueueResult(call: ResultCall<T>): Result<T> {
        var result: Result<T>? = null
        call.enqueue(object : Callback<Result<T>> {
            override fun onResponse(call: Call<Result<T>>, response: Response<Result<T>>) {
                result = response.body()
            }

            override fun onFailure(call: Call<Result<T>>, t: Throwable) = Unit
        })
        return checkNotNull(result)
    }

    @Test
    fun noContentIsSuccessForUnit() {
        val call = ResultCall(FakeCall(Response.success<Unit>(204, null)), isUnit = true)

        assertEquals(Result.success(Unit), enqueueResult(call))
        assertEquals(Result.success(Unit), call.execute().body())
    }

    @Test
    fun emptyBodyIsFailureForOtherTypes() {
        val call = ResultCall(FakeCall(Response.success<String>(204, null)))

        assertTrue(enqueueResult(call).isFailure)
        assertTrue(call.execute().body()!!.isFailure)
    }

    @Test
    fun bodyIsSuccess() {
        val call = ResultCall(FakeCall(Response.success("value")))

        assertEquals(Result.success("value"), enqueueResult(call))
    }
}
