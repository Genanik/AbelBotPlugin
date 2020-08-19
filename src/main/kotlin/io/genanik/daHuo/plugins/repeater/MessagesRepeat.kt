package io.genanik.daHuo.plugins.repeater

import io.genanik.daHuo.utils.isEqualWithRemoveMsgSource
import io.genanik.daHuo.utils.removeMsgSource
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*


/**
 * 判断出现两条相同内容后 将内容镜像并返回镜像的MessageChain
 * 构造时需要传入GroupMessageEvent
 */
class MessagesRepeat(message: MessageChain) {
    private var lastMessage = message.removeMsgSource() // 保证没有MsgSource
    private var times = 1
    private var needTimes = 2
    private var repeatTimes = 0

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

        val tmp = newMessage.toString()
        // 过滤mirai不支持的消息类型
        if (tmp.indexOf("暂不支持查看")!=-1) {
            return false
        }
        // 屏蔽QQ商城表情
        if (tmp[0] =='[' && tmp[tmp.length-1] == ']'){
            return false
        }

        return result
    }

    // MessageChain倒序
    suspend fun textBackRepeat(oldMsgChain: MessageChain, contact: Contact): MessageChain {
        val newMsgChain = MessageChainBuilder() // 发送的MsgChain

//        oldMsgChain.removeMsgSource().reversed().forEach {
        oldMsgChain.removeMsgSource().forEach {
            val msgClip = it.asMessageChain()

            // 识别委托
            val pic: Image? by msgClip.orNull()
//            val text: PlainText? by msgClip.orNull()

            // 根据委托处理信息
            newMsgChain.processImg(pic, contact)
//            newMsgChain.processText(text)

            if (!hasBeenProcessed){
                // 没有被处理委托
                newMsgChain.add(it)
            }
            hasBeenProcessed = false
        }
        return newMsgChain.asMessageChain()
    }
}