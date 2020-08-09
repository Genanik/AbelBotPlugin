package io.genanik.daHuo.plugins

import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.ResizePic
import io.genanik.daHuo.utils.getAllPicture
import io.genanik.daHuo.utils.isGIF
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*

class ImageResize {

    fun trigger(abelPM: AbelPlugins, controller: GroupMessageSubscribersBuilder){
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