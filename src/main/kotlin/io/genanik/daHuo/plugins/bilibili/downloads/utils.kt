package io.genanik.daHuo.plugins.bilibili.downloads

import java.net.HttpURLConnection
import java.net.URL

fun GetExt(uri: String): String {
    val url = URL(uri)
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
    val contentType = con.contentType
    return contentType.split("/")[1]
}

fun Size(url: String): Long {
    val con: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
    return con.contentLength.toLong()
}

fun MatchOf(text: String, pattern: String): List<String> {
    var value: List<String>
    var re: Regex = pattern.toRegex()
    value = (re.find(text) ?: return emptyList()).groupValues
    if (value.isNotEmpty()) {
        return value
    }
    return emptyList()
}