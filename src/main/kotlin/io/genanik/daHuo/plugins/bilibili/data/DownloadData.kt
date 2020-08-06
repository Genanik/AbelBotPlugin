package io.genanik.daHuo.plugins.bilibili.data

data class tokenData (
    val token: String
)

data class token (
    val code: Int,
    val message: String,
    val data: tokenData
)

data class bangumiEpData (
    val aid: Int,
    val cid: Int,
    val id: Int,
    val ep_id: Int
)

data class bangumiData (
    val epInfo: bangumiEpData,
    val epList: List<bangumiEpData>
)

data class videoPagesData (
    val cid: Int,
    val part: String,
    val page: Int
)

data class multiPageVideoData (
    val title: String,
    val pages: List<videoPagesData>
)

data class multiPage (
    val aid: Int,
    val videoData: multiPageVideoData
)

data class dashStream (
    val id: Int,
    val baseUrl: String,
    val bandwidth: Int
)

data class dashStreams (
    val video: List<dashStream>,
    val audio: List<dashStream>
)

data class dURL (
    val size: Long,
    val url: String
)

data class dashInfo (
    val quality: Int,
    val accept_description: List<String>,
    val accept_quality: List<Int>,
    val dash: dashStreams,
    val durl: List<dURL>
)

data class dash (
    val code: Int,
    val message: String,
    val data: dashInfo,
    val result: dashInfo
)

var qualityString = mapOf(
    120 to "超清 4K",
    116 to "高清 1080P60",
    74  to "高清 720P60",
    112 to "高清 1080P+",
    80  to "高清 1080P",
    64  to "高清 720P",
    48  to "高清 720P",
    32  to "清晰 480P",
    16  to "流畅 360P",
    15  to "流畅 360P"
)
