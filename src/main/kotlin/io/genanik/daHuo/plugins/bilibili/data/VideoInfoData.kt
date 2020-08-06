package io.genanik.daHuo.plugins.bilibili.data

data class BiliInfResponse(
    val data: BiliInfData
)

data class BiliInfData(
    val bvid: String,
    val aid: Int,
    val pic: String,
    val title: String,
    val owner: BilOwner,
    val stat: BiliStat
)

data class BilOwner(
    val name: String
)

data class BiliStat(
    val view: Int,      // 播放
    val danmaku: Int,   // 弹幕
    val favorite: Int,  // 收藏
    val like: Int       // 点赞
)