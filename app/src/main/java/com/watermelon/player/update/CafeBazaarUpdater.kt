package com.watermelon.player.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.annotations.SerializedName

data class UpdateInfo(
    @SerializedName("versionCode") val versionCode: Int,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("downloadUrl") val downloadUrl: String,
    @SerializedName("changelog") val changelog: String
)

object CafeBazaarUpdater {
    private const val BAZAAR_PACKAGE = "com.farsitel.bazaar"

    fun checkUpdates(context: Context, onUpdateAvailable: (UpdateInfo) -> Unit) {
        // Check via Bazaar API or intent
        val bazaarIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("bazaar://details?id=com.watermelon.player.ir")
            `package` = BAZAAR_PACKAGE
        }

        try {
            context.startActivity(bazaarIntent)
        } catch (e: Exception) {
            // Fallback to web
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://cafebazaar.ir/app/com.watermelon.player.ir")
            }
            context.startActivity(webIntent)
        }
    }
}
