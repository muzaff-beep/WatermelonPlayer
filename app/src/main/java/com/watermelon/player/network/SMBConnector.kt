package com.watermelon.player.network

import com.hierynomus.mssmb2.SMB2Dialect
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.share.DiskShare
import java.net.InetSocketAddress

class SMBConnector {
    private val client = SMBClient()
    private var connection: Connection? = null
    private var share: DiskShare? = null

    fun connect(host: String, port: Int = 445, username: String, password: String): Boolean {
        return try {
            val auth = AuthenticationContext(username, password.toCharArray(), null)
            connection = client.connect(InetSocketAddress(host, port))
            connection!!.authenticate(auth)
            share = (connection!!.connectShare("IPC$") as DiskShare)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun listFiles(path: String = ""): List<String> {
        share?.list(path)?.forEach { file ->
            if (file.isDirectory) {
                // Add folder
            } else if (isVideoFile(file.name)) {
                // Add video file
            }
        }
        return emptyList()
    }

    private fun isVideoFile(name: String): Boolean = name.matches(Regex(".*\\.(mp4|mkv|avi)$"))

    fun disconnect() {
        share?.close()
        connection?.close()
        client.close()
    }
}
