package io.genanik.daHuo.plugins.bilibili

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.data.BiliLightAppData
import io.genanik.daHuo.plugins.bilibili.data.UniversalLightApp
import net.mamoe.mirai.message.data.LightApp

class BiliLightApp(app: LightApp){

    private val content = app.content
    private var isBiliLightApp = false
    private lateinit var biliJsonBean: BiliLightAppData

    init {
        val prompt = Gson().fromJson(content, UniversalLightApp::class.javaObjectType).prompt
        if (prompt.contains("哔哩哔哩")){
            isBiliLightApp = true
            biliJsonBean = Gson().fromJson(content, BiliLightAppData::class.javaObjectType)
        }
    }

    private fun getUrlOfNull(): String? {
        if (!isBiliLightApp){
            return null
        }
        return biliJsonBean.meta.detail_1.qqdocurl
    }

    fun getId(): String {
        var url = getUrlOfNull()
        return if (url != null){
            url = shortToLongLink(url)
            return getAvBvFromNormalLink(url)
        }else{
            // 搜索
            "aid=av" + SearchVideo(biliJsonBean.meta.detail_1.desc)
                .getAid()
        }
    }

}