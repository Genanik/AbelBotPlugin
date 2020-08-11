package io.genanik.daHuo.plugins

import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.ResizePic
import io.genanik.daHuo.utils.getAllPicture
import io.genanik.daHuo.utils.isGIF
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*
import io.genanik.daHuo.abel.AbelPluginBase as AbelPluginBase

class ImageResize(aPlugins: AbelPlugins) : AbelPluginBase(aPlugins) {

    private val abelPM = aPlugins
    override val name = "图片缩放"
    override val description = "@Abel机器人并加上\"放大\"或\"缩小\"一个或多个静态图，可以缩放静态图"
    override val version = "0.1.0"

    override fun trigger(controller: GroupMessageSubscribersBuilder) {
        controller.atBot {
            // 图片缩放是否开启
            if (!abelPM.getStatus("图片缩放", this.group.id)) {
                return@atBot
            }
            // 是否不是GIF
            val firstImg: Image = message.firstIsInstanceOrNull() ?: return@atBot
            if (isGIF( firstImg.queryUrl())) {
                return@atBot
            }
            // 有没有文字
            val firstText: PlainText = message.firstIsInstanceOrNull() ?: return@atBot
            // 图片缩放
            val newMsg = MessageChainBuilder()
            val allPic = getAllPicture(message)
            allPic.forEach { picUrl ->
                // toBig
                var tmp = firstText.content.indexOf("放大") != -1
                if (tmp) {
                    newMsg.add(ResizePic(picUrl).ToBigger(group))
                }
                // toSmall
                tmp = firstText.content.indexOf("缩小") != -1
                if (tmp) {
                    newMsg.add(ResizePic(picUrl).ToSmaller(group))
                }
            }
            reply(newMsg.asMessageChain())
        }
    }

}