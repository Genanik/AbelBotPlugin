package io.genanik.daHuo.plugins.bilibili.downloads

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.data.dashInfo
import io.genanik.daHuo.plugins.bilibili.data.multiPage

fun genParts(dashData: dashInfo, quality: Int): List<TypesPart>? {
    val parts = mutableListOf<TypesPart>()
    if (dashData.dash.audio == null ){
        throw Exception("audio是空的")
    } else {
        var checked = false
        for (stream in dashData.dash.video){
            if (stream.id == quality) {
                val s = Size(stream.baseUrl)

                parts.add(TypesPart(stream.baseUrl, s, "mp4"))
                checked = true
                break
            }
        }

        if (!checked) {
            return null
        }
    }
    return parts
}

fun getMultiPageData(html: String): multiPage? {
//    var multiPageDataString: List<String> = MatchOf(html, """window.__INITIAL_STATE__=(.+?);\(function""".toRegex())
    var multiPageDataString: List<String> = MatchOf(html, """window\.__INITIAL_STATE__=(.+?);\(function""".toRegex())
    return Gson().fromJson(multiPageDataString[1], multiPage::class.java)
}

// NeedDownloadList return the indices of playlist that need download
fun NeedDownloadList(items: String, itemStart: Int, itemEnd: Int, length: Int): List<Int> {
    var newItemStart = itemStart
    var newitemEnd = itemEnd
    if (items != "") {
        var itemList = mutableListOf<Int>()
        var selStart: Int
        var selEnd: Int
        val temp = items.split(",")

        for ( i in temp) {
            val selection = i.split("-")
            val selStart = selection[0].replace(" ", "").toInt()

            selEnd = if (selection.size >= 2) {
                selection[1].toInt()
            } else {
                selStart
            }

            for (item in 0..selEnd) {
                itemList.add(item)
            }
        }
        return itemList
    }

    if (itemStart < 1) {
        newItemStart = 1
    }
    if (itemEnd == 0) {
        newitemEnd = length
    }
    if (newitemEnd < newItemStart) {
        newitemEnd = itemStart
    }
    val range = mutableListOf<Int>()
    for (i in newItemStart..newitemEnd){
        range.add(i)
    }
    return range
}