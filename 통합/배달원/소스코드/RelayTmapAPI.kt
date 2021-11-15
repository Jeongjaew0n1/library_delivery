package com.example.reigster_show

import android.content.ContentValues
import android.util.Log
import java.net.HttpURLConnection
import java.lang.StringBuffer
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.io.IOException

class RelayTmapAPI {
    fun request(_url: String?, _params: ContentValues?): String? {
        var urlConn: HttpURLConnection? = null
        val sbParams = StringBuffer()
        sbParams.append("")
        try {
            val url = URL(_url)
            urlConn = url.openConnection() as HttpURLConnection
            urlConn.requestMethod = "POST"
            urlConn!!.setRequestProperty("Accept-Charset", "UTF-8")
            urlConn.setRequestProperty(
                "Context_Type",
                "application/x-www-form-urlencoded;cahrset=UTF-8"
            )
            Log.d("RequestHttp(sbParams)", sbParams.toString())
            if (urlConn.responseCode != HttpURLConnection.HTTP_OK) return null
            val reader = BufferedReader(InputStreamReader(urlConn.inputStream, "UTF-8"))
            Log.d("RequestHttp(urlConn.getInputStream())", urlConn.inputStream.toString())

            var line: String?
            var page: String? = ""
            while (reader.readLine().also { line = it } != null) {
                page += line
            }
            Log.d("RequestHttp(page)", page!!)
            return page
        } catch (e: MalformedURLException) { // for URL.
            e.printStackTrace()
        } catch (e: IOException) { // for openConnection().
            e.printStackTrace()
        } finally {
            urlConn?.disconnect()
        }
        return null
    }
}