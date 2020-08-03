package io.genanik.daHuo.plugins

import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.isEqualWithRemoveMsgSource
import io.genanik.daHuo.utils.mirrorImage
import io.genanik.daHuo.utils.removeMsgSource
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.*

class MessageRepeater{
    private val msgRepeatController = mutableMapOf<Long, MessagesRepeat>()

    fun trigger(abelPM: AbelPlugins, controller: GroupMessageSubscribersBuilder){
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


/**
 * 判断出现两条相同内容后 将内容镜像并返回镜像的MessageChain
 * 构造时需要传入GroupMessageEvent
 */
class MessagesRepeat(message: MessageChain) {
    private var lastMessage = message.removeMsgSource() // 保证没有MsgSource
    private var times = 1
    private var needTimes = 2
    private var repeatTimes = 0
    private var hasBeenProcessed = false

    // 更新缓存并返回是否复读
    fun update(newMessage: MessageChain): Boolean{

        val isSame = newMessage.isEqualWithRemoveMsgSource(lastMessage)

        if (isSame) { // 当前消息与上一条消息内容相同
            times++
        }

        val result = times == needTimes
        lastMessage = newMessage.removeMsgSource() // 保证没有MsgSource

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