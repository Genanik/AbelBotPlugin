package io.genanik.miraiPlugin

import io.genanik.miraiPlugin.utils.isEqual
import io.genanik.miraiPlugin.utils.mirrorImage
import io.genanik.miraiPlugin.utils.removeMsgSource
import io.genanik.miraiPlugin.utils.translate.Method
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * 判断出现两条相同内容后 将内容镜像并返回镜像的MessageChain
 * 构造时需要传入GroupMessageEvent
 */
class MessagesRepeatFunction(message: GroupMessageEvent) {

    private var lastMessage: GroupMessageEvent = message // 不保证没有MessageSource块
    private var times = 1
    private var needTimes = 2
    private var repeatTimes = 0
    private var hasBeenProcessed = false

    // 更新缓存并返回是否复读
    fun update(newMessage: GroupMessageEvent): Boolean{

        val isSame = newMessage.message.isEqual(lastMessage.message)

        if (isSame) { // 当前消息与上一条消息内容相同
            times++
        }

        val result = times == needTimes
        lastMessage = newMessage

        if (result){
            repeatTimes++
            needTimes += 3
            times = 1
        }

        if (!isSame){
            repeatTimes = 0
            needTimes = 2
            times = 1
        }

        return result
    }

    // MessageChain倒序
    suspend fun textBackRepeat(oldMsgChain: MessageChain, contact: Contact): MessageChain {
        val newMsgChain = MessageChainBuilder() // 发送的MsgChain

        oldMsgChain.removeMsgSource().reversed().forEach {
            val msgClip = it.asMessageChain()

            // 识别委托
            val pic: Image? by msgClip.orNull()
            val text: PlainText? by msgClip.orNull()

            // 根据委托处理信息
            newMsgChain.processImg(pic, contact)
            newMsgChain.processText(text)

            if (!hasBeenProcessed){
                // 没有被处理委托
                newMsgChain.add(it)
            }
            hasBeenProcessed = false
        }
        return newMsgChain.asMessageChain()
    }

    private suspend fun MessageChainBuilder.processImg(pic: Image?, contact: Contact) {
        if (pic == null){
            return
        }
        this.add(mirrorImage(pic.queryUrl(), contact))
        hasBeenProcessed = true
    }

    private fun MessageChainBuilder.processText(text: PlainText?) {
        if (text == null){
            return
        }
        val src = text.toString()
        val symbolRaw = arrayOf('[', ']', '(', ')', '（', '）', '{', '}', '【', '】', '「', '」', '“', '”', '/', '\\', '‘', '’', '<', '>', '《', '》')
        val symbolNew = arrayOf(']', '[', ')', '(', '）', '（', '}', '{', '】', '【', '」', '「', '”', '“', '\\', '/', '’', '‘', '>', '<', '》', '《')

        //1.得到代码点数量，也即是实际字符数，注意和length()的区别
        //举例：
        //一个emoji表情是一个字符，codePointCount()是1，length()是2。
        val cpCount = src.codePointCount(0, src.length)

        //2.得到字符串的第一个代码点index，和最后一个代码点index
        //举例：比如3个emoji表情，那么它的cpCount=3；firCodeIndex=0；lstCodeIndex=4
        //因为每个emoji表情length()是2，所以第一个是0-1，第二个是2-3，第三个是4-5
        val firCodeIndex = src.offsetByCodePoints(0, 0)
        val lstCodeIndex = src.offsetByCodePoints(0, cpCount - 1)
        var result = ""
        var index = firCodeIndex
        while (index <= lstCodeIndex) {
            //3.获得代码点，判断是否是emoji表情
            //注意，codePointAt(int) 这个int对应的是codeIndex
            //举例:3个emoji表情，取第3个emoji表情，index应该是4
            val codepoint = src.codePointAt(index)
            result = if (isEmojiCharacter(codepoint)) {
                // 特殊字符
                val length = if (Character.isSupplementaryCodePoint(codepoint)) 2 else 1
                src.substring(index, index + length) + result
            } else {
                // 普通字符
                val rawChar = codepoint.toChar()
                val symbolInt = symbolRaw.findOrNull(rawChar)
                if (symbolInt != null) {
                    symbolNew[symbolInt] + result
                }else{
                    rawChar + result
                }
            }
            //4.确定指定字符（Unicode代码点）是否在增补字符范围内。
            //因为除了表情，还有些特殊字符也是在增补字符方位内的。
            index += if (Character.isSupplementaryCodePoint(codepoint)) 2 else 1
        }
        this.add(result)
        
        hasBeenProcessed = true
    }
    
    private fun isEmojiCharacter(codePoint: Int): Boolean {
        return (codePoint in 0x2600..0x27BF // 杂项符号与符号字体
                || codePoint == 0x303D || codePoint == 0x2049 || codePoint == 0x203C || codePoint in 0x2000..0x200F //
                || codePoint in 0x2028..0x202F //
                || codePoint == 0x205F //
                || codePoint in 0x2065..0x206F //
                /* 标点符号占用区域 */
                || codePoint in 0x2100..0x214F // 字母符号
                || codePoint in 0x2300..0x23FF // 各种技术符号
                || codePoint in 0x2B00..0x2BFF // 箭头A
                || codePoint in 0x2900..0x297F // 箭头B
                || codePoint in 0x3200..0x32FF // 中文符号
                || codePoint in 0xD800..0xDFFF // 高低位替代符保留区域
                || codePoint in 0xE000..0xF8FF // 私有保留区域
                || codePoint in 0xFE00..0xFE0F // 变异选择器
                || codePoint >= 0x10000) // Plane在第二平面以上的，char都不可以存，全部都转
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

    var sentence = arrayListOf<String>()
    val taowa = arrayListOf<String>()

    init {
        taowa.add("套娃")
//        taowa.add("禁止套娃")
//        taowa.add("禁止")
    }

    private fun textStruct(singleWord: String): String{
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
        val result = sentence.random()
        sentence = arrayListOf()
        return result
    }

    fun TrumpTextWithoutNPL(input: String): String {
        if (input.length > 5){
            return "这个关键词太长了_(:з」∠)_"
        }
        if (check(input)){
            return "禁止套娃！"
        }
        return textStruct(input)

    }

    private fun check(targetString: String): Boolean {
        var isTaoWa = false
        for (i in taowa){
            if (targetString.indexOf(i) != -1){
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