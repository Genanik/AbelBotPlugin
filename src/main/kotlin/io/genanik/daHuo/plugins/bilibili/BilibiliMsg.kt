package io.genanik.daHuo.plugins.bilibili

import io.genanik.daHuo.abel.AbelPluginBase
import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.plugins.bilibili.downloads.Extract
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*

class BilibiliMsg(aPlugins: AbelPlugins) : AbelPluginBase(aPlugins) {

    private val abelPM = aPlugins

    override fun trigger(controller: GroupMessageSubscribersBuilder) {
        controller.always {
            // 是否开启
            if (!abelPM.getStatus("bilibili", this.group.id)) {
                return@always
            }
            // bilibili
            val id = getAvBvFromMsg(message) ?: return@always
            val biliVideo = VideoInf(id)

            // info
            reply(biliVideo.beautyMsg(group))

            // link
//            reply(formatDL(biliVideo.avLink))
        }
    }

    private fun formatDL(biliUrl: String): MessageChain {
        val streams = Extract(biliUrl, "").getStreams()
        val result = MessageChainBuilder()

        streams.forEach {
            result.add("${it[0]}: ") // 画质
            for (i in 1 until it.size){
                result.add("${it[i]} ") // 链接
            }
            result.add("\n")
        }
        return result.asMessageChain()
    }

}