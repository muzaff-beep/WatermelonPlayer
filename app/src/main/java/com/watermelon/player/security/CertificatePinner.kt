package com.watermelon.player.security

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

class CertificatePinner {
    private val pinner = CertificatePinner.Builder()
        .add("cdn.watermelon.tv", "sha256/YOUR_CERT_HASH")
        .add("api.opensubtitles.org", "sha256/OPENSUBTITLES_HASH")
        .build()

    fun applyToClient(client: OkHttpClient.Builder): OkHttpClient.Builder {
        return client.certificatePinner(pinner)
    }
}
