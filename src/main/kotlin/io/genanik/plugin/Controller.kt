package io.genanik.plugin

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.simpleloader.Util.mirrorImage
import io.genanik.plugin.Util.translate.Method
import net.mamoe.mirai.utils.toExternalImage
import net.mamoe.mirai.utils.upload
import java.text.SimpleDateFormat
import java.util.*

/**
 * 判断出现两条相同内容后 将内容镜像并返回镜像的MessageChain
 * 构造时需要传入一条GroupMessage
 */
class MessagesRepeatController (message: GroupMessage) {

    private var lastMessage: GroupMessage = message

    // 返回null的时候不要复读
    fun update(newMessage: GroupMessage): Boolean{
        return if (
            removeMessageSource(lastMessage.message).toString() ==
            removeMessageSource(newMessage.message).toString()  ){
            lastMessage = newMessage
            true
        } else {
            lastMessage = newMessage
            false
        }
    }

    fun removeMessageSource(message: MessageChain): MessageChain{
        var tmp = MessageChainBuilder()
        message.forEach {
            if (!it.toString().contains("[mirai:source:")){
                tmp.add(it)
            }
        }
        return tmp.asMessageChain()
    }

    // MessageChain倒序 祖传配方，懒得重写了
    suspend fun textBackRepeat(oldMsgChain: MessageChain, contact: Contact): MessageChain {
        var newMsgChain = MessageChainBuilder()
        removeMessageSource(oldMsgChain).reversed().forEach { messageClip ->
            if (messageClip.toString().contains("mirai:")) {
                // 特殊消息
                var pic = oldMsgChain.firstIsInstanceOrNull<Image>()
                if (pic != null){
                    newMsgChain.add(mirrorImage(pic.queryUrl()).toExternalImage().upload(contact))
                }else{
                    newMsgChain.add(messageClip)
                }
            } else {
                //文字消息
                var tmp = ""
                messageClip.toString().forEach {
                    tmp = it + tmp
                }
                newMsgChain.add(tmp)
            }
        }
        return newMsgChain.asMessageChain()
    }

}

/**
 * 返回翻译的繁体内容
 */
class MessagesTranslateController{

    fun autoTranslate(rawMessage: GroupMessage): MessageChain{
        return translate(rawMessage)
    }

    private fun translate(rawMessage: GroupMessage): MessageChain{
        // 构造 MessageChain
        val replyMsg = MessageChainBuilder()
        var arMsg: String
        var isNeedSend = false
        // 写入数据
        rawMessage.message.forEach {
            if ((it.toString().indexOf("mirai:") != -1 ) or (it.toString().indexOf("@") != -1)) {
                // 不启动翻译
                replyMsg.add(it)
            }else{ // 启动翻译
                val text = it.toString()

                arMsg = Method().localTranslate(text) //离线翻译

                if (!arMsg.contains("Error with code")){
                    if (text != arMsg){
                        replyMsg += arMsg
                        isNeedSend = true
                    }
                }
            }
            // if完了
        }
        return if (isNeedSend and !replyMsg.equals(MessageChainBuilder().asMessageChain())){
            replyMsg.asMessageChain() // 返回被翻译过的MsgChain
        }else{
            MessageChainBuilder().asMessageChain() // 没有翻译过 返回空MsgChain
        }
    }

}

/**
 * 返回当前时间
 */
class TellTheTimeOnTheDotController{
    fun getNow(): String {
        return SimpleDateFormat("HH:mm").format(Date())
    }
}

/**
 * 输入关键字，返回一条名人名言（伪
 */
class DonaldTrumpController{

    private fun textStruct(singleWord:String): String{
        var sentence = arrayListOf<String>()
        sentence.add("我们有全球最好的$singleWord" + "专家\n——特朗普")
        sentence.add("对于$singleWord，没什么需要恐慌的\n——特朗普")
        sentence.add("有些人会说我非常 非常非常有才，特别是$singleWord"+"方面\n——特朗普")
        sentence.add("没有人比我特朗普更懂$singleWord")
        return sentence.random()
    }

    fun isAtBot(msg: MessageChain, miraiBot: Bot):Boolean {
        var at:At? = msg.firstIsInstanceOrNull()
        if (at != null){
            if (at.target == miraiBot.id){
                return true
            }
        }
        return false
    }

    fun TrumpTextWithoutNPL(input: String): String {
        return textStruct(input)
    }
}