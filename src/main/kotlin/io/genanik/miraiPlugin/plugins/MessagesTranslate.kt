package io.genanik.miraiPlugin.plugins

import io.genanik.miraiPlugin.settings.AbelPluginsManager
import io.genanik.miraiPlugin.utils.translate.Method
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.forEachContent

/**
 * 返回翻译的繁体内容
 */
class MessagesTranslate {

    fun trigger(abelPM: AbelPluginsManager, controller: GroupMessageSubscribersBuilder){
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

    fun translate(rawMessage: GroupMessageEvent): MessageChain {
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