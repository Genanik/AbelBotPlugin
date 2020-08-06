package io.genanik.daHuo.plugins.bilibili

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.data.BiliSearchRequest
import io.genanik.daHuo.utils.get

class SearchVideo(keyword: String) {

    private var param: String = "keyworld=$keyword"

    fun getAid(): Int {
        // 返回第一个内容的aid
        val requestBody = get("https://api.bilibili.com/x/web-interface/search/all/v2?$param")
        val bean = Gson().fromJson(requestBody, BiliSearchRequest::class.javaObjectType)
        return bean.data.result[0].data[0].id
    }

}