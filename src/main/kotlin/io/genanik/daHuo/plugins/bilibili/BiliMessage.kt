package io.genanik.daHuo.plugins.bilibili

import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

fun getAvBvFromNormalLink(link: String): String{
    val rules = Regex("""bilibili\.com\/video\/(?:[Aa][Vv]([0-9]+)|([Bb][Vv][0-9a-zA-Z]+))""")
    val search = rules.find(link) ?: throw Exception("[Bilibili] 获取视频ID错误")
    return if (search.groupValues[1] == ""){
        "bvid=${search.groupValues[2]}"
    }else{
        "aid=${search.groupValues[1]}"
    }
}

fun shortToLongLink(shortLink: String): String {
    var conn: HttpURLConnection? = null
    try {
        conn = URL(shortLink).openConnection() as HttpURLConnection
    } catch (e: IOException) {
        e.printStackTrace()
    }
    conn!!.instanceFollowRedirects = false
    conn!!.connectTimeout = 5000
    val url: String = conn.getHeaderField("Location")
    conn.disconnect()
    return url
}

fun getAvBvFromMsg(msg: MessageChain): String? {
    val plainText: PlainText = msg.firstIsInstanceOrNull() ?: return null
    var maybeLink = plainText.content

    // normalLink
    var rule = Regex("""bilibili\.com\/video\/(?:[Aa][Vv]([0-9]+)|([Bb][Vv][0-9a-zA-Z]+))""")
    if (rule.containsMatchIn(maybeLink)){
        return getAvBvFromNormalLink(maybeLink)
    }

    // shortLink
    rule = Regex("""(b23|acg)\.tv\/[0-9a-zA-Z]+""")
    if (rule.containsMatchIn(maybeLink)){
        maybeLink = shortToLongLink(maybeLink)
        return getAvBvFromNormalLink(maybeLink)
    }

    // app
    val app = getApp(msg) ?: return null
    return BiliLightApp(app).getId()
}

fun getApp(msg: MessageChain): LightApp? {
    return msg.firstIsInstanceOrNull() ?: return null
}