package com.watermelon.player.subtitle

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.watermelon.player.config.EditionManager

/**
 * SubtitleBypassEngine.kt
 * Purpose: Securely fetches subtitles from blocked sites using a rotating proxy chain.
 * Iran-first design:
 *   - Only active in Global edition (Iran edition = 100% offline, no net permission)
 *   - Proxy list is encrypted and embedded in assets/raw/subtitle_proxies.json
 *   - Auto-rotates proxies on failure
 *   - No logging of URLs or IPs
 *   - Timeout aggressive (10s) to prevent hanging on blocked connections
 *   - Fallback to local/embedded subtitles if all proxies fail
 */

object SubtitleBypassEngine {

    // Proxy list will be loaded from encrypted JSON at runtime
    private var proxyList: List<String> = emptyList()
    private var currentProxyIndex = 0

    // OkHttp client with conservative timeouts – reused for efficiency
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Initialize engine – called once from WatermelonApp.onCreate()
     * Decrypts and loads proxy list from assets
     */
    suspend fun initialize(context: Context) {
        // Only load proxies if NOT Iran edition (Iran has no internet permission)
        if (EditionManager.isIranEdition) {
            proxyList = emptyList()
            return
        }

        withContext(Dispatchers.IO) {
            try {
                // Read encrypted raw resource
                val encryptedJson = context.resources.openRawResource(
                    com.watermelon.player.R.raw.subtitle_proxies
                ).readBytes()

                // Simple XOR decryption (key hardcoded – obfuscate in ProGuard)
                val key = "WatermelonIran2026".toByteArray()
                val decrypted = encryptedJson.mapIndexed { i, byte ->
                    (byte.toInt() xor key[i % key.size]).toByte()
                }.toByteArray()

                // Decode Base64 then parse as JSON array of strings
                val jsonString = String(Base64.decode(decrypted, Base64.DEFAULT))
                proxyList = jsonString.split(",").map { it.trim('"', ' ') }

            } catch (e: Exception) {
                // Silent fail – proxy list remains empty
                proxyList = emptyList()
            }
        }
    }

    /**
     * Fetch subtitle file from given direct URL using current proxy
     * Rotates proxy on failure
     */
    suspend fun fetchSubtitle(url: String): String? {
        if (EditionManager.isIranEdition || proxyList.isEmpty()) {
            return null // No net in Iran
        }

        repeat(proxyList.size) {
            val proxyUrl = proxyList[currentProxyIndex]
            try {
                val request = Request.Builder()
                    .url("$proxyUrl?url=${java.net.URLEncoder.encode(url, "UTF-8")}")
                    .header("User-Agent", "WatermelonPlayer/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful && response.body != null) {
                        return response.body!!.string()
                    }
                }
            } catch (e: IOException) {
                // Rotate to next proxy on failure
            }

            // Move to next proxy
            currentProxyIndex = (currentProxyIndex + 1) % proxyList.size
        }

        return null // All proxies failed
    }

    /**
     * Check if engine is operational (has proxies and not Iran edition)
     */
    fun isAvailable(): Boolean = !EditionManager.isIranEdition && proxyList.isNotEmpty()
}
