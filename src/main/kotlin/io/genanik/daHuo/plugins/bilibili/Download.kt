package io.genanik.daHuo.plugins.bilibili

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.data.*
import io.genanik.daHuo.utils.get
import java.lang.System.err
import java.net.HttpURLConnection
import java.net.URL

var utoken: String = ""

fun genAPI(aid: Int, cid: Int, quality: Int, bangumi: Boolean, cookie: String): String? {
    var baseAPIURL: String
    var params: String

    if (cookie != "" && utoken == "") {
        utoken = get("${bilibiliTokenAPI}aid=$aid&cid=$cid")
        var t = Gson().fromJson(utoken, token::class.javaObjectType)

        if (t.code != 0) {
            return null
        }
        utoken = t.data.token
    }

    var api: String
    if (bangumi) {
        // The parameters need to be sorted by name
        // qn=0 flag makes the CDN address different every time
        // quality=120(4k) is the highest quality so far
        params = "cid=$cid&bvid=&qn=$quality&type=&otype=json&fourk=1&fnver=0&fnval=16"
        baseAPIURL = bilibiliBangumiAPI
    } else {
        params = "avid=$aid&cid=$cid&bvid=&qn=$quality&type=&otype=json&fourk=1&fnver=0&fnval=16"
        baseAPIURL = bilibiliAPI
    }
    api = baseAPIURL + params
    // bangumi utoken also need to put in params to sign, but the ordinary video doesn't need
    if (!bangumi && utoken != "") {
        api = "$api&utoken=$utoken"
    }
    return api
}

fun genParts(dashData: dashInfo, quality: Int, referer: String): List<Part>? {
    val parts = mutableListOf<Part>()
    if (dashData.dash.audio == null ){
        val url = dashData.durl[0].url
        val ext = GetNameAndExt(url)

        parts[0] = Part(url, dashData.durl[0].size, ext)
    } else {

        var checked = false
        for (stream in dashData.dash.video){
            if (stream.id == quality) {
                val s = Size(stream.baseUrl, referer)

                parts[0] = Part(stream.baseUrl, s, "mp4")
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

data class Part (
    val url: String,
    val size: Long,
    val ext: String
)

data class bilibiliOptions (
    val url :String,
    val html: String,
    val bangumi: Boolean,
    val aid: Int,
    val cid: Int,
    val page: Int,
    var subtitle: String
)

fun GetNameAndExt(uri: String): String {
    val url = URL(uri)
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
    val contentType = con.contentType
    return contentType.split("/")[1]
}

fun Size(url: String, refer: String=""): Long {
    val con: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
    return con.contentLength.toLong()
}

// Data is the main data structure for the whole video data.
data class TypesData (
    // URL is used to record the address of this download
    val url: String,
    val site: String,
    val title: String,
    val type: String,
    // each stream has it's own Parts and Quality
    val streams: Map<String, Stream>,
    // danmaku, subtitles, etc
    val caption: TypesPart,
    // Err is used to record whether an error occurred when extracting the list data
    val err: Error?
)

fun MatchOneOf(text: String, patterns: List<String>): List<String> {
    var re: Regex
    var value: List<String>
    for ( pattern in patterns) {
        re = pattern.toRegex()
        value = (re.find(text) ?: return emptyList()).groupValues
        if (value.isNotEmpty()) {
            return value
        }
    }
    return emptyList()
}

fun getMultiPageData(html: String): multiPage? {
    var multiPageDataString: List<String> = MatchOneOf(html, listOf("""window.__INITIAL_STATE__=(.+?);\(function"""))
    return Gson().fromJson(multiPageDataString[1], multiPage::class.java)
}

fun extractNormalVideo(url: String, html: String, extractOption: TypesOptions): List<TypesData>? {
    val pageData = getMultiPageData(html) ?: return null
    if (err != null) {
        return null
    }
    if (!extractOption.Playlist) {
        // handle URL that has a playlist, mainly for unified titles
        // <h1> tag does not include subtitles
        // bangumi doesn't need this
        val pageString = MatchOneOf(url, listOf("""\?p=(\d+)"""))
        var p = if (pageString == null) {
            // https://www.bilibili.com/video/av20827366/
            1
        } else {
            // https://www.bilibili.com/video/av20827366/?p=2
            pageString[1].toInt()
        }

        if (pageData.videoData.pages.size < p || p < 1) {
            return throw Exception("ErrURLParseFailed")
        }

        val page = pageData.videoData.pages[p - 1]
        val options = bilibiliOptions(url, html, false, pageData.aid, page.cid, p, "")
        // "part":"" or "part":"Untitled"
        if (page.part == "Untitled" || pageData.videoData.pages.size == 1) {
            options.subtitle = ""
        } else {
            options.subtitle = page.part
        }
        return listOf(bilibiliDownload(options, extractOption) ?: return null)
    }

    // handle normal video playlist
    // https://www.bilibili.com/video/av20827366/?p=1
    val needDownloadItems = NeedDownloadList(
        extractOption.Items,
        extractOption.ItemStart,
        extractOption.ItemEnd,
        pageData.videoData.pages.size
    )
    val extractedData = mutableListOf<TypesData>()
    val wgp = extractOption.ThreadNumber
    val dataIndex = 0
    for ((index, u) in pageData.videoData.pages.withIndex()) {
        if (ItemInSlice(index + 1, needDownloadItems)) {
            continue
        }
        val options = bilibiliOptions(url, html, false, pageData.aid, u.cid, u.page, u.part)
        extractedData[index] = bilibiliDownload(options, extractOption) ?: return null
        return extractedData
    }
    return null
}

// Extract is the main function to extract the data.
fun Extract(url: String, option: TypesOptions): List<TypesData>? {
    val html = get(url)
    // handle normal video
    return extractNormalVideo(url, html, option)
}


// Stream is the data structure for each video stream, eg: 720P, 1080P.
data class Stream (
    // eg: "1080"
    val id: String,
    // eg: "1080P xxx"
    val quality: String,
    // [Part: {URL, Size, Ext}, ...]
    // Some video stream have multiple parts,
    // and can also be used to download multiple image files at once
    val parts: List<Part>,
    // total size of all urls
    val size: Long,
    // the file extension after video parts merged
    val ext: String,
    // if the parts need mux
    val NeedMux: Boolean
)

data class TypesOptions (
    // Playlist indicates if we need to extract the whole playlist rather than the single video.
    val Playlist: Boolean,
    // Items defines wanted items from a playlist. Separated by commas like: 1,5,6,8-10.
    val Items: String,
    // ItemStart defines the starting item of a playlist.
    val ItemStart: Int,
    // ItemEnd defines the ending item of a playlist.
    val ItemEnd: Int,

    // ThreadNumber defines how many threads will use in the extraction, only works when Playlist is true.
    val ThreadNumber: Int,
    val Cookie: String,

    // EpisodeTitleOnly indicates file name of each bilibili episode doesn't include the playlist title
    val EpisodeTitleOnly: Boolean
)

fun bilibiliDownload(options: bilibiliOptions, extractOption: TypesOptions): TypesData? {
    var html = ""
    html = if (options.html != "") {
        // reuse html string, but this can't be reused in case of playlist
        options.html
    } else {
        get(options.url)
    }

    // Get "accept_quality" and "accept_description"
    // "accept_description":["高清 1080P","高清 720P","清晰 480P","流畅 360P"],
    // "accept_quality":[120,112,80,48,32,16],
    val api = genAPI(options.aid, options.cid, 120, options.bangumi, extractOption.Cookie) ?: return null
    var jsonString = get(api)

    var data = Gson().fromJson(jsonString, dash::class.java)
    var dashData: dashInfo
    if (data.data.accept_description == null) {
        dashData = data.result
    } else {
        dashData = data.data
    }

    var audioPart = Part("",0L, "")
    if (dashData.dash.audio != null) {
        // Get audio part
        var audioID = 0
        var audios = mutableMapOf<Int, String>()
        var bandwidth = 0
        for (stream in dashData.dash.audio) {
            if (stream.bandwidth > bandwidth) {
                audioID = stream.id
            }
            audios[stream.id] = stream.baseUrl
            bandwidth = stream.bandwidth
        }
        val s = Size(audios[audioID]!!)
        audioPart = Part(audios[audioID]!!, s, "m4a",)
    }

    val streams = mutableMapOf<String, Stream>()
    for ( q in dashData.quality) {
        // Avoid duplicate streams
        val ok = streams[q]
        if (ok != null) {
            continue
        }
        val api = genAPI(options.aid, options.cid, q, options.bangumi, extractOption.Cookie) ?: return null

        jsonString = get(api)

        data = Gson().fromJson(jsonString, dash::class.java)
        dashData = if (data.data.accept_description == null) {
            data.result
        } else {
            data.data
        }
        val parts = (genParts(dashData, q, options.url) ?: continue).toMutableList()

        var size: Long = 0L
        for (part in parts) {
            size += part.size
        }
        if (audioPart != null) {
            parts[parts.size] = audioPart
        }
        streams["$q"] = Stream(q.toString(), qualityString[q]!!,parts, size, "",false)
    }

    // TODO site title
    return TypesData("url", "哔哩哔哩 bilibili.com", "title" , "video", streams, TypesPart(
        "https://comment.bilibili.com/options.cid.xml", 0L,"xml"), null)
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

fun ItemInSlice(item: Int, list: List<Int>): Boolean {
    for (i in 0..list.size) {
        if (item == list[i]) {
            return true
        }
    }
    return false
}

data class TypesPart (
    val url: String,
    val size: Long,
    val ext: String
)