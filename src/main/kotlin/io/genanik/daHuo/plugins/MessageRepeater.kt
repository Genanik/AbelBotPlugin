package io.genanik.daHuo.plugins

import io.genanik.daHuo.abel.AbelPluginBase
import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.repeater.MessagesRepeat
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder

class MessageRepeater(aPlugins: AbelPlugins) : AbelPluginBase(aPlugins) {

    private val abelPM = aPlugins
    override val name = "复读"
    override val description = "同一条消息出现两次后，Abel机器人自动跟读"
    override val version = "0.1.0"

    private val msgRepeatController = mutableMapOf<Long, MessagesRepeat>()

    override fun trigger(controller: GroupMessageSubscribersBuilder) {
        controller.always {
            if (!abelPM.getStatus("复读", this.group.id)) {
                return@always
            }
            if (msgRepeatController.contains(this.group.id)) {
                // 更新msgRepeat内容
                if (msgRepeatController[this.group.id]!!.update(this.message)) {
                    reply(msgRepeatController[this.group.id]!!.textBackRepeat(this.message, this.group))
                }
            } else {
                // 为本群创建一个msgRepeat
                msgRepeatController[this.group.id] = MessagesRepeat(this.message)
            }
        }
    }
}