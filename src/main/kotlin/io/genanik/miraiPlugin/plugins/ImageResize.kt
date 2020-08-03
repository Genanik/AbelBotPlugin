package io.genanik.miraiPlugin.plugins

import io.genanik.miraiPlugin.AbelPluginMain
import io.genanik.miraiPlugin.Settings.AbelPluginsManager
import io.genanik.miraiPlugin.utils.ResizePic
import io.genanik.miraiPlugin.utils.getAllPicture
import io.genanik.miraiPlugin.utils.isGIF
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*

class ImageResize {

    fun trigger(abelPM: AbelPluginsManager, controller: GroupMessageSubscribersBuilder){
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
            // 图片缩放
            val newMsg = MessageChainBuilder()
            val allPic = getAllPicture(message)
            allPic.forEach { picUrl ->
                val maybeText = message.firstOrNull(PlainText) ?: return@atBot
                val isToBig = maybeText.content.indexOf("放大") != -1
                if (isToBig) {
                    newMsg.add(ResizePic(picUrl).ToBigger(group))
                } else {
                    newMsg.add(ResizePic(picUrl).ToSmaller(group))
                }
            }
            reply(newMsg.asMessageChain())
        }
    }

}