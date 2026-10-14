package com.humboshot.koillection

import android.app.Application
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.auth.AuthStore
import kotlin.concurrent.thread

class MainApplication : Application(), ImageLoaderFactory {
    init {
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: MainApplication
            private set

        val applicationContext: Context
            get() {
                return INSTANCE.applicationContext
            }
    }

    override fun onCreate() {
        super.onCreate()
        thread(name = "auth-load") { AuthStore.load() }
    }

    override fun newImageLoader(): ImageLoader {
        val builder = ImageLoader.Builder(this)
            .okHttpClient { ApiClient.okHttpClient }
            .crossfade(true)
            .respectCacheHeaders(false)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.1)
                    .build()
            }
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }

                add(SvgDecoder.Factory())
            }

        if (BuildConfig.DEBUG) builder.logger(DebugLogger())

        return builder.build()
    }
}