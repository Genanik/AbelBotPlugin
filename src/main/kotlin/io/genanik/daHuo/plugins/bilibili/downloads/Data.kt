package io.genanik.daHuo.plugins.bilibili.downloads

data class bilibiliOptions (
    val url :String,
    val html: String,
    val bangumi: Boolean,
    val aid: Int,
    val cid: Int,
    val page: Int,
    var subtitle: String
)

// Stream is the data structure for each video stream, eg: 720P, 1080P.
data class Stream (
    // eg: "1080"
    val id: String,
    // eg: "1080P xxx"
    val quality: String,
    // [Part: {URL, Size, Ext}, ...]
    // Some video stream have multiple parts,
    // and can also be used to download multiple image files at once
    val parts: List<TypesPart>,
    // total size of all urls
    val size: Long,
    // the file extension after video parts merged
    val ext: String,
    // if the parts need mux
    val NeedMux: Boolean
)