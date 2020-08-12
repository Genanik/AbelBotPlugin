package io.genanik.daHuo.plugins.translate

import io.genanik.daHuo.abel.AbelPluginBase
import io.genanik.daHuo.abel.AbelPlugins
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.forEachContent

/**
 * 返回翻译的繁体内容
 */
class MessagesTranslate(aPlugins: AbelPlugins) : AbelPluginBase(aPlugins) {

    override val name = "翻译"
    override val description = "自动翻译包含繁体的消息"
    override val version = "0.1.0"

    override fun trigger(controller: GroupMessageSubscribersBuilder) {
        controller.always {
            if (abelPM.getStatus("翻译", this.group.id)) { // 默认不开启
                return@always
            }
            val tmp = translate(this)
            if ((tmp.toString() != "")) {
                reply(tmp)
            }
        }
    }

    private fun translate(rawMessage: GroupMessageEvent): MessageChain {
        // 构造 MessageChain
        val replyMsg = MessageChainBuilder()
        var arMsg: String
        var isNeedSend = false
        // 写入数据
        rawMessage.message.forEachContent {
            if ((it.toString().indexOf("mirai:") != -1 ) or (it.toString().indexOf("@") != -1)) {
                // 不启动翻译
                replyMsg.add(it)
            }else{ // 启动翻译
                val text = it.toString()

                arMsg = Method().localTranslate(text) // 离线翻译
                if (text != arMsg){
                    replyMsg += arMsg
                    isNeedSend = true
                }
            }
            // if完了
        }
        return if (isNeedSend and !replyMsg.equals(EmptyMessageChain)){
            replyMsg.asMessageChain() // 返回被翻译过的MsgChain
        }else{
            EmptyMessageChain// 没有翻译过 返回空MsgChain
        }
    }

}