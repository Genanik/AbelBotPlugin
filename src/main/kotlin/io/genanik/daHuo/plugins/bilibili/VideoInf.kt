package io.genanik.daHuo.plugins.bilibili

import com.google.gson.Gson
import io.genanik.daHuo.plugins.bilibili.data.BiliInfData
import io.genanik.daHuo.plugins.bilibili.data.BiliInfResponse
import io.genanik.daHuo.utils.get
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.uploadAsImage
import java.net.URL

class VideoInf(id: String) {

    private var param: String = id
    private var data: BiliInfData

    init {
        val requestBody = get(ViewAPI + param)
        data = Gson().fromJson(requestBody, BiliInfResponse::class.javaObjectType).data
    }

    suspend fun beautyMsg(contact: Contact): MessageChain {
        val result = MessageChainBuilder()
        result.add( URL(data.pic).uploadAsImage(contact) + "\n" )
        result.add( data.title + "\n")
        result.add( "UP: ${data.owner.name}\n" )
        result.add( "${toHumanNum(data.stat.view)}播放 ${toHumanNum(data.stat.danmaku)}弹幕\n" )
        result.add( "https://www.bilibili.com/video/av${data.aid}" )
        return result.asMessageChain()
    }

    private fun toHumanNum(num: Int): String {
        return if (num >= 10000) {
            val numStr = num.toDouble()
            String.format("%.2f",(numStr/10000)) + "万"
        } else {
            num.toString()
        }
    }

}