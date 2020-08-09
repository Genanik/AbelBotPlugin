package io.genanik.daHuo.plugins.bilibili.downloads

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.*
import io.genanik.daHuo.plugins.bilibili.data.dash
import io.genanik.daHuo.plugins.bilibili.data.dashInfo
import io.genanik.daHuo.plugins.bilibili.data.qualityString
import io.genanik.daHuo.utils.get

class Extract {

    // Extract is the main function to extract the data.
    fun Extract(url: String, option: TypesOptions): List<TypesData>? {
        val html = get(url)
        return extractNormalVideo(url, html, option)
    }

    private fun extractNormalVideo(url: String, html: String, extractOption: TypesOptions): List<TypesData>? {
        val pageData = getMultiPageData(html) ?: return null
        if (System.err != null) {
            return null
        }
        if (!extractOption.Playlist) {
            // handle URL that has a playlist, mainly for unified titles
            // <h1> tag does not include subtitles
            // bangumi doesn't need this
            val pageString = MatchOf(url, """\?p=(\d+)""")
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
            if (needDownloadItems.contains(index + 1)) {
                continue
            }
            val options = bilibiliOptions(url, html, false, pageData.aid, u.cid, u.page, u.part)
            extractedData[index] = bilibiliDownload(options, extractOption) ?: return null
            return extractedData
        }
        return null
    }


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

}