package io.genanik.daHuo.plugins.bilibili.data

data class UniversalLightApp (
    val prompt: String
)

data class BiliLightAppData (
    val meta: BiliMeta,
    val prompt: String
)

data class BiliMeta (
    val detail_1: BiliDetail
)

data class BiliDetail (
    val desc: String, // 标题
    val qqdocurl: String? // 可能存在的短链接
)