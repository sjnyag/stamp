package com.sjn.stamp.utils

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.support.v4.media.session.MediaSessionCompat
import fi.iki.elonen.NanoHTTPD
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

object LocalCastHelper {
    private val TAG = LogHelper.makeLogTag(LocalCastHelper::class.java)

    class HttpServer @Throws(IOException::class)
    internal constructor(internal val context: Context, internal val wifiAddress: String, internal val port: Int) : NanoHTTPD(port) {
        internal var media: MediaSessionCompat.QueueItem? = null

        val url = "http://$wifiAddress:$port"

        override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            if (media == null) {
                return NanoHTTPD.Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "No music")
            }
            return when {
                session.uri.contains("image") -> serveImage()
            //session.uri.contains("debug") -> NanoHTTPD.Response(NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, media?.description?.mediaUri?.toString())
                else -> serveMusic()
            }
        }

        private fun serveMusic(): NanoHTTPD.Response = NanoHTTPD.Response(
                Response.Status.OK, "audio/mp3",
                try {
                    FileInputStream(media!!.description.mediaUri!!.toString())
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    null
                })

        private fun serveImage(): NanoHTTPD.Response = NanoHTTPD.Response(
                Response.Status.OK, "image/jpeg",
                try {
                    context.contentResolver.openInputStream(Uri.parse(media?.description?.iconUri?.toString()))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    null
                })
    }

    fun startSever(context: Context): HttpServer? {
        try {
            val wifiAddress = getWifiAddress(context)
            val port = LocalCastHelper.findOpenPort(wifiAddress, 8080)
            LogHelper.i(TAG, "http://$wifiAddress:$port")
            val httpServer = LocalCastHelper.HttpServer(context, wifiAddress, port)
            httpServer.start()
            return httpServer
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getWifiAddress(context: Context): String {
        val ipAddress = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress
        return (ipAddress and 0xFF).toString() + "." +
                (ipAddress shr 8 and 0xFF) + "." +
                (ipAddress shr 16 and 0xFF) + "." +
                (ipAddress shr 24 and 0xFF)
    }


    private fun findOpenPort(ip: String, startPort: Int): Int {
        (startPort..65535)
                .filter { isPortAvailable(ip, it, 200) }
                .forEach { return it }
        throw RuntimeException("There is no open port.")
    }

    private fun isPortAvailable(ip: String, port: Int, timeout: Int): Boolean =
            try {
                Socket().apply {
                    connect(InetSocketAddress(ip, port), timeout)
                    close()
                }
                false
            } catch (e: Exception) {
                true
            }
}