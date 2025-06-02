package com.humboshot.koillection.api.core

import okhttp3.Request
import okio.IOException
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class ResultCall<T>(val delegate: Call<T>) : Call<Result<T>> {
    override fun enqueue(callback: Callback<Result<T>>) {
        delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T?>, response: Response<T?>) {
                    if (response.isSuccessful) {
                        callback.onResponse(
                            this@ResultCall,
                            Response.success(
                                response.code(),
                                Result.success(response.body()!!)
                            )
                        )
                    } else {
                        callback.onResponse(
                            this@ResultCall,
                            Response.success(
                                Result.failure(Exception("Error: ${response.code()} \n Message: ${response.message()}"))
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<T?>, throwable: Throwable) {
                    val errorMessage = when (throwable) {
                        is IOException -> "No internet connection"
                        is HttpException -> "Something went wrong! ${throwable.message}"
                        else -> throwable.localizedMessage
                    }

                    callback.onResponse(
                        this@ResultCall,
                        Response.success(Result.failure(RuntimeException(errorMessage, throwable)))
                    )
                }
            }
        )
    }

    override fun execute(): Response<Result<T>?> {
        return Response.success(Result.success(delegate.execute().body()!!))
    }

    override fun isExecuted(): Boolean {
        return delegate.isExecuted
    }

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean {
        return delegate.isCanceled
    }

    override fun clone(): Call<Result<T>> {
        return ResultCall(delegate.clone())
    }

    override fun request(): Request {
        return delegate.request()
    }

    override fun timeout(): Timeout {
        return delegate.timeout()
    }
}