package com.watermelon.player.datasource

import androidx.media3.common.DataSourceException
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient

class NetworkDataSource : OkHttpDataSource.Factory {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun createDataSource(): OkHttpDataSource = OkHttpDataSource.Factory(client).createDataSource()
}
