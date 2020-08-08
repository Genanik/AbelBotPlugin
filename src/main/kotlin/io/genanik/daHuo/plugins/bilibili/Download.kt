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
    val subtitle: String
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
    val streams: Stream,
    // danmaku, subtitles, etc
    val caption: Part,
    // Err is used to record whether an error occurred when extracting the list data
    val err: Error?
)

fun extractBangumi(url: String, html: String, extractOption: TypesOptions): TypesData {
    val dataString = MatchOneOf(html, listOf("""window.__INITIAL_STATE__=(.+?);\(function`)"""))[1]
    var data: bangumiData
    data = Gson().fromJson(dataString, bangumiData::class.java)
    if (!extractOption.Playlist) {
        var aid = data.epInfo.aid
        var cid = data.epInfo.cid
        if (aid <= 0 || cid <= 0) {
            aid = data.epList[0].aid
            cid = data.epList[0].cid
        }
        val options = bilibiliOptions(url, html, true, aid, cid, 0, "")
        val biliOpt = bilibiliDownload(options, extractOption) ?: throw Exception("No data")
//         []*types.Data{}, nil

        return TypesData(biliOpt.url, biliOpt.site, biliOpt.title, biliOpt.type, biliOpt.streams, biliOpt.caption, null)
    }

    // handle bangumi playlist
    needDownloadItems := utils.NeedDownloadList(extractOption.Items, extractOption.ItemStart, extractOption.ItemEnd, len(data.EpList))
    extractedData := make([]*types.Data, len(needDownloadItems))
    wgp := utils.NewWaitGroupPool(extractOption.ThreadNumber)
    dataIndex := 0
    for index, u := range data.EpList {
        if !utils.ItemInSlice(index+1, needDownloadItems) {
            continue
        }
        wgp.Add()
        id := u.EpID
        if id == 0 {
            id = u.ID
        }
        // html content can't be reused here
        options := bilibiliOptions{
        url:     fmt.Sprintf("https://www.bilibili.com/bangumi/play/ep%d", id),
        bangumi: true,
        aid:     u.Aid,
        cid:     u.Cid,
    }
        go func(index int, options bilibiliOptions, extractedData []*types.Data) {
        defer wgp.Done()
        extractedData[index] = bilibiliDownload(options, extractOption)
    }(dataIndex, options, extractedData)
        dataIndex++
    }
    wgp.Wait()
    return extractedData, nil
}

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

func getMultiPageData(html string) (*multiPage, error) {
    var data multiPage
    multiPageDataString := utils.MatchOneOf(
    html, `window.__INITIAL_STATE__=(.+?);\(function`,
    )
    if multiPageDataString == nil {
        return &data, errors.New("this page has no playlist")
    }
    err := json.Unmarshal([]byte(multiPageDataString[1]), &data)
    if err != nil {
        return nil, err
    }
    return &data, nil
}

func extractNormalVideo(url, html string, extractOption types.Options) ([]*types.Data, error) {
    pageData, err := getMultiPageData(html)
    if err != nil {
        return nil, err
    }
    if !extractOption.Playlist {
        // handle URL that has a playlist, mainly for unified titles
        // <h1> tag does not include subtitles
        // bangumi doesn't need this
        pageString := utils.MatchOneOf(url, `\?p=(\d+)`)
        var p int
        if pageString == nil {
            // https://www.bilibili.com/video/av20827366/
            p = 1
        } else {
            // https://www.bilibili.com/video/av20827366/?p=2
            p, _ = strconv.Atoi(pageString[1])
        }

        if len(pageData.VideoData.Pages) < p || p < 1 {
            return nil, types.ErrURLParseFailed
        }

        page := pageData.VideoData.Pages[p-1]
        options := bilibiliOptions{
            url:  url,
            html: html,
            aid:  pageData.Aid,
            cid:  page.Cid,
            page: p,
    }
        // "part":"" or "part":"Untitled"
        if page.Part == "Untitled" || len(pageData.VideoData.Pages) == 1 {
            options.subtitle = ""
        } else {
            options.subtitle = page.Part
        }
        return []*types.Data{bilibiliDownload(options, extractOption)}, nil
    }

    // handle normal video playlist
    // https://www.bilibili.com/video/av20827366/?p=1
    needDownloadItems := utils.NeedDownloadList(extractOption.Items, extractOption.ItemStart, extractOption.ItemEnd, len(pageData.VideoData.Pages))
    extractedData := make([]*types.Data, len(needDownloadItems))
    wgp := utils.NewWaitGroupPool(extractOption.ThreadNumber)
    dataIndex := 0
    for index, u := range pageData.VideoData.Pages {
        if !utils.ItemInSlice(index+1, needDownloadItems) {
            continue
        }
        wgp.Add()
        options := bilibiliOptions{
            url:      url,
            html:     html,
            aid:      pageData.Aid,
            cid:      u.Cid,
            subtitle: u.Part,
            page:     u.Page,
    }
        go func(index int, options bilibiliOptions, extractedData []*types.Data) {
        defer wgp.Done()
        extractedData[index] = bilibiliDownload(options, extractOption)
    }(dataIndex, options, extractedData)
        dataIndex++
    }
    wgp.Wait()
    return extractedData, nil
}

func New() types.Extractor {
    return &extractor{}
}

// Extract is the main function to extract the data.
func (e *extractor) Extract(url string, option types.Options) ([]*types.Data, error) {
    var err error
    html, err := request.Get(url, referer, nil)
    if err != nil {
        return nil, err
    }
    if strings.Contains(url, "bangumi") {
        // handle bangumi
        return extractBangumi(url, html, option)
    }
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

    return Data(
        Site:    "哔哩哔哩 bilibili.com",
        Title:   title,
        Type:    types.DataTypeVideo,
        Streams: streams,
        Caption: &types.Part{
        URL: fmt.Sprintf("https://comment.bilibili.com/%d.xml", options.cid),
        Ext: "xml",
        ),
        URL: options.url,
    }
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
