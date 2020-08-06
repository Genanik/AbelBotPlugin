package io.genanik.daHuo.plugins.bilibili

import com.google.gson.Gson
import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.get
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class BilibiliMsg {

    fun trigger(abelPM: AbelPlugins, controller: GroupMessageSubscribersBuilder){
        controller.always {
            // 是否开启
            if (!abelPM.getStatus("bilibili", this.group.id)) {
                return@always
            }
            // bilibili
            val id = getAvBvFromMsg(message) ?: return@always
            reply(VideoInf(id).beautyMsg(group))
        }
    }

}