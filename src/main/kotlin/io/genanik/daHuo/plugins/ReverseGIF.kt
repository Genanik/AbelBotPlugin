package io.genanik.daHuo.plugins

import io.genanik.daHuo.abel.AbelPluginBase
import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.getAllPicture
import io.genanik.daHuo.utils.isGIF
import io.genanik.daHuo.utils.reverseImage
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*

/**
 * 输入关键字，返回一条名人名言（伪
 */
class ReverseGIF(aPlugins: AbelPlugins) : AbelPluginBase(aPlugins) {

    private val abelPM = aPlugins

    override fun trigger(controller: GroupMessageSubscribersBuilder) {
        controller.atBot {
            // 倒转GIF是否开启
            if (!abelPM.getStatus("倒转GIF", this.group.id)) {
                return@atBot
            }
            // 是否为GIF
            val firstImg: Image = message.firstIsInstanceOrNull() ?: return@atBot
            if (!isGIF( firstImg.queryUrl() )) {
                return@atBot
            }
            // 倒转GIF
            val newMsg = MessageChainBuilder()
            val allPic = getAllPicture(message)

            allPic.forEach { picUrl ->
                newMsg.add(reverseImage(picUrl, group))
            }
            reply(newMsg.asMessageChain())
        }
    }

}