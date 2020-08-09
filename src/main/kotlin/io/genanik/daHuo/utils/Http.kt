package io.genanik.daHuo.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

fun get(url: String): String {
    // 去get
    val con: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
    con.requestMethod = "GET"
    val `in` = BufferedReader(InputStreamReader(con.inputStream))
    var inputLine: String?
    val response = StringBuffer()

    while (`in`.readLine().also { inputLine = it } != null) {
        response.append(inputLine)
    }
    `in`.close()

    return response.toString()
}

fun getHttp(url: String): String {
    var connection: HttpURLConnection
    var response = StringBuilder()

    try {
        val tmp = URL(url).openConnection()
        tmp.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Safari/605.1.15")
        tmp.setRequestProperty("Referer", "https://www.bilibili.com")
        tmp.setRequestProperty("contentType", "UTF-8")
        tmp.setRequestProperty("Accept-Charset", "UTF-8")
        connection = tmp as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8000
        connection.readTimeout = 8000

        var gZIPInputStream: GZIPInputStream
        val encoding: String? = connection.contentEncoding
        if (encoding == "gzip") {
            gZIPInputStream = GZIPInputStream(connection.inputStream)
            val bufferedReader = BufferedReader(InputStreamReader(gZIPInputStream))
            var line: String? = null
            while (bufferedReader.readLine().also { line = it } != null) {
                //转化为UTF-8的编码格式
                line = String(line!!.toByteArray(charset("UTF-8")))
                response.append(line)
            }
            bufferedReader.close()
        } else {
            val bufferedReader =
                BufferedReader(InputStreamReader(connection.inputStream))
            var line: String? = null
            while (bufferedReader.readLine().also { line = it } != null) {
                //转化为UTF-8的编码格式
                line = String(line!!.toByteArray(charset("UTF-8")))
                response.append(line)
            }
            bufferedReader.close()
        }
        return response.toString()

    }catch (ex: Exception) {
        throw ex
    }
}