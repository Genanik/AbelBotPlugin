package io.genanik.miraiPlugin

import io.genanik.miraiPlugin.uttil.mirrorImage
import io.genanik.miraiPlugin.uttil.removeMessageSource
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import io.genanik.miraiPlugin.uttil.translate.Method
import java.text.SimpleDateFormat
import java.util.*

/**
 * 判断出现两条相同内容后 将内容镜像并返回镜像的MessageChain
 * 构造时需要传入GroupMessageEvent
 */
class MessagesRepeatFunction (message: GroupMessageEvent) {

    private var lastMessage: GroupMessageEvent = message // 不保证没有MessageSource块
    private var times = 1
    private var needTimes = 2

    // 更新缓存并返回是否复读
    fun update(newMessage: GroupMessageEvent): Boolean{

        val newMsgWithoutMessageSource = removeMessageSource(newMessage.message)

        if (removeMessageSource(lastMessage.message).toString() == newMsgWithoutMessageSource.toString()){// 当前消息与上一条消息内容相同
                times++
        }else{
            needTimes = 2
        }
        return if (times == needTimes){
            // 确定要复读了
            lastMessage = newMessage
            times = 1
            needTimes = 6
            true
        } else {
            lastMessage = newMessage
            false
        }
    }

    // MessageChain倒序 祖传配方，懒得重写了
    suspend fun textBackRepeat(oldMsgChain: MessageChain, contact: Contact): MessageChain {
        val newMsgChain = MessageChainBuilder()
        removeMessageSource(oldMsgChain).reversed().forEach { messageClip ->
            if (messageClip.toString().contains("mirai:")) {
                // 特殊消息
                try {
                    val pic = messageClip as Image
                    val newImg = mirrorImage(pic.queryUrl(), contact)
                    newMsgChain.add(newImg)
                }catch (e: Exception){
                    // 不是图片
                    newMsgChain.add(messageClip)
                }
            } else {
                //文字消息
                var tmp = ""
                val symbolRaw = arrayOf('[', ']', '(', ')', '（', '）', '{', '}', '【', '】', '「', '」', '“', '”', '/', '\\', '‘', '’', '<', '>', '《', '》')
                val symbolNew = arrayOf(']', '[', ')', '(', '）', '（', '}', '{', '】', '【', '」', '「', '”', '“', '\\', '/', '’', '‘', '>', '<', '》', '《')
                messageClip.toString().forEach {
                    // 字符替换
                    val symbolInt = symbolRaw.findOrNull(it)

                    tmp = when {
                        symbolInt != null -> {
                            "" + symbolNew[symbolInt] + tmp
                        }
                        else -> {
                            it + tmp
                        }
                    }
                }
                newMsgChain.add(tmp)
            }
        }
        return newMsgChain.asMessageChain()
    }

    private fun <Char> Array<Char>.findOrNull(targetChar: Char): Int? {
        for ((index, i) in this.withIndex()){
            if (i == targetChar){
                return index
            }

        }
        return null
    }

}

/**
 * 返回翻译的繁体内容
 */
class MessagesTranslateFunction {

    fun translate(rawMessage: GroupMessageEvent): MessageChain{
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

/**
 * 返回当前时间
 */
class TimeFunction {
    fun getNow(): String {
        return SimpleDateFormat("HH:mm").format(Date())
    }
}

/**
 * 输入关键字，返回一条名人名言（伪
 */
class DonaldTrumpFunction {

    val sentence = arrayListOf<String>()
    val taowa = arrayListOf<String>()

    init {
        taowa.add("套娃")
        taowa.add("禁止套娃")
        taowa.add("禁止")
    }

    private fun textStruct(singleWord:String): String{
        sentence.add("我们有全球最好的${singleWord}专家\n——特朗普")
        sentence.add("对于${singleWord}，没什么需要恐慌的\n——特朗普")
        sentence.add("有些人会说我非常 非常非常有才，特别是${singleWord}方面\n——特朗普")
        sentence.add("没有人比我特朗普更懂${singleWord}")
        sentence.add("人们并没有真的从无到有创造出什么，而是重新组合创造出更多东西，例如${singleWord}\n——特朗普")
        sentence.add("特朗普式${singleWord}，欲盖而弥彰！")
        sentence.add("总统越是否认${singleWord}，民众就越应该关心${singleWord}")
        sentence.add("我认为${singleWord}是不可避免的\n——特朗普")
        sentence.add("${singleWord}并不可怕\n——特朗普")
        sentence.add("你不握手，怎么能是${singleWord}呢？\n——特朗普")
        sentence.add("把${singleWord}当作流感就好\n——特朗普")
        return sentence.random()
    }

    fun TrumpTextWithoutNPL(input: String): String {
        if (input.length > 6){
            return "这个关键词太长了_(:з」∠)_"
        }
        val result = textStruct(input)
        if (sentence.check(input)){
            return "禁止套娃！"
        }
        return result
    }

    private fun <String> ArrayList<String>.check(targetString: String): Boolean {
        var isTaoWa = false
        for (i in taowa){
            if (i == targetString){
                isTaoWa = true
            }
        }
        return isTaoWa
    }

}

/**
 * 从http://114.67.100.226:45777/?key=ForMscWeekily 中获取聊天文本并返回词频
 */
class MscChatTermFrequency {

}