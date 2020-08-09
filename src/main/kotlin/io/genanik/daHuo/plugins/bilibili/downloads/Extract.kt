package io.genanik.daHuo.plugins.bilibili.downloads

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.*
import io.genanik.daHuo.plugins.bilibili.data.dash
import io.genanik.daHuo.plugins.bilibili.data.dashInfo
import io.genanik.daHuo.plugins.bilibili.data.qualityString
import io.genanik.daHuo.utils.get
import io.genanik.daHuo.utils.getHttp

class Extract(url: String, biliCookie: String) {
    private var dataList :List<TypesData>
    init {
        val html = getHttp(url)
        dataList = extractNormalVideo(url, html, biliCookie) ?: throw Exception("无法解析视频")
    }

    fun getStreams(): List<List<String>> {
        val fullList = mutableListOf<List<String>>()
        // [0]  指视频质量
        // [>0] 指视频链接
        var singleList = mutableListOf<String>()

        dataList.forEach { data ->
            for( key in data.streams.keys){
                // 单个stream
                val stream = data.streams[key] ?: continue
                singleList.add(stream.quality)
                for ((index,part) in stream.parts.withIndex()){
                    singleList.add(part.url)
                }
                fullList.add(singleList)
                singleList = mutableListOf()
            }
        }
        return fullList
    }

    private fun extractNormalVideo(url: String, html: String, biliCookie: String): List<TypesData>? {
        val pageData = getMultiPageData(html) ?: return null
        val extractedData = mutableListOf<TypesData>()
        for ((index, u) in pageData.videoData.pages.withIndex()) {
            val options = bilibiliOptions(url, html, false, pageData.aid, u.cid, u.page, u.part)
            extractedData.add(bilibiliDownload(options, biliCookie) ?: return null)
            return extractedData
        }
        return null
    }


    private fun bilibiliDownload(options: bilibiliOptions, biliCookie: String): TypesData? {
        var html = ""
        html = if (options.html != "") {
            // reuse html string, but this can't be reused in case of playlist
            options.html
        } else {
            get(options.url)
        }
        var biliAPI = Downloads("")


        // Get "accept_quality" and "accept_description"
        // "accept_description":["高清 1080P","高清 720P","清晰 480P","流畅 360P"],
        // "accept_quality":[120,112,80,48,32,16],
        val api = biliAPI.genAPI(options.aid, options.cid, 120, options.bangumi, biliCookie) ?: return null
        var jsonString = getHttp(api)

        var data = Gson().fromJson(jsonString, dash::class.java)
        var dashData: dashInfo
        dashData = if (data.data.accept_description == null) {
            return null
        } else {
            data.data
        }
        var audioPart = TypesPart("",0L, "")
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
            audioPart = TypesPart(audios[audioID]!!, s, "m4a",)
        }

        val streams = mutableMapOf<String, Stream>()
        val q = dashData.quality
        // Avoid duplicate streams
        val newApi = biliAPI.genAPI(options.aid, options.cid, q, options.bangumi, biliCookie) ?: return null

        jsonString = getHttp(newApi)

        data = Gson().fromJson(jsonString, dash::class.java)
        dashData = if (data.data.accept_description == null) {
            return null
        } else {
            data.data
        }
        val parts = genParts(dashData, q)!!.toMutableList()

        var size = 0L
        for (part in parts) {
            size += part.size
        }
        if (audioPart != null) {
            parts.add(audioPart)
        }
        streams["$q"] = Stream(q.toString(), qualityString[q]?: return null, parts, size, "",false)

        return TypesData("url", "哔哩哔哩 bilibili.com", "title" , "video", streams, TypesPart(
            "https://comment.bilibili.com/options.cid.xml", 0L,"xml"), null)
    }

}