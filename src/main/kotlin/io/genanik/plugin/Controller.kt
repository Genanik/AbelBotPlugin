package io.genanik.plugin

import io.genanik.plugin.Settings.debug
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.simpleloader.Util.mirrorImage
import io.genanik.plugin.Util.translate.Method
import net.mamoe.mirai.utils.toExternalImage
import net.mamoe.mirai.utils.upload
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessagesRepeatController {

    private var repeatDisableList = ArrayList<Long>()
    private var lastMessageMap = mutableMapOf<Long, GroupMessage>()
    private var lastRepeatMessageMap = mutableMapOf<Long, MessageChain>()

    private fun updateMessage(groupID: Long, str: GroupMessage) {
        when (groupID){
            135023273L -> println("跳过记录 135023273")
            else -> {
                lastMessageMap[groupID] = str
                if (!lastMessageMap.containsKey(groupID)){
                    lastRepeatMessageMap[groupID] = MessageChainBuilder().asMessageChain()
                }
            }
        }
    }

    private fun getLastMessage(groupID: Long): GroupMessage {
        return lastMessageMap[groupID]!!
    }

    private fun updateLastRepeatMessage(groupID: Long, str: MessageChain) {
        lastRepeatMessageMap[groupID] = str
    }

    private fun getLastRepeatMessage(groupID: Long): MessageChain {
        if (lastRepeatMessageMap[groupID] != null){
            return lastRepeatMessageMap[groupID]!!
        }
        return MessageChainBuilder().asMessageChain()
    }

    fun updateRepeatOnAndOff(groupID: Long, state: Boolean) {
        if (state){
            repeatDisableList.remove(groupID)
        }else{
            repeatDisableList.add(groupID)
        }
    }

    private fun getRepeatOnAndOff(groupID: Long): Boolean {
        return repeatDisableList.contains(groupID)
    }

    //判断是否需要复读
    fun isNowRepeat(newMessage: GroupMessage): Boolean {
        if (getRepeatOnAndOff(newMessage.group.id)) {
            // 这个群已开启复读
            if (!lastMessageMap.containsKey(newMessage.group.id)){
                // 这个群之前说话没被机器人记录
                updateMessage(newMessage.group.id, newMessage)
                return false
            }
            return if (newMessage.message == getLastMessage(newMessage.group.id).message) {
                // 此消息等于在该群的上一条消息
                updateMessage(newMessage.group.id, newMessage)
                updateLastRepeatMessage(newMessage.group.id, newMessage.message)
                true
            }else{
                // 内容与上一条不符
                updateMessage(newMessage.group.id, newMessage)
                false
            }
        }else{
            // 没开启复读
            return false
        }
    }

    // MessageChain倒序
    suspend fun textBackRepeat(oldMsgChain: MessageChain, contact: Contact): MessageChain {
        var newMsgChain = MessageChainBuilder()
        oldMsgChain.reversed().forEach { messageClip ->
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

    // backup
    /*
    suspend fun textBackRepeat(oldMsgChain: MessageChain, contact: Contact): MessageChain {
        var newMsgChain = MessageChainBuilder()
        oldMsgChain.reversed().forEach {
            if (it.toString().contains("mirai:")) {
                // 特殊消息
                var pic = oldMsgChain.firstOrNull<Image>()
                if (pic != null){
                    var raw = pic.queryUrl()
                    if (isGif(raw)){
                        // true
                        newMsgChain.add(mirrorGif(raw,"output").toExternalImage().upload(contact))
                    }else{
                        // false
                        newMsgChain.add(
                            mirror(ImageIO.read(URL(raw)))
                                .toExternalImage().upload(contact))
                    }
                }else{
                    newMsgChain.add(it)
                }

            } else {
                //文字消息
                var tmp = ""
                it.toString().forEach {
                    tmp = it + tmp
                }
                newMsgChain.add(tmp)
            }
        }
    }
     */

}

class MessagesTranslateController{

//    private var translateMethodMap = mutableMapOf<Long, Boolean>()
//
//    fun init(groups: ContactList<Group>) {
//        groups.forEach {
//            translateMethodMap[it.id] = false
//        }
//    }
//
//    fun updateMethodMap(groupID: Long, useBaidu: Boolean){
//        translateMethodMap[groupID] = useBaidu
//    }
//
//    private fun getTranslateMethod(groupID: Long): Boolean {
//        return translateMethodMap[groupID]!!
//    }
//
//    fun updateTransOnAndOff(group: Long, state: Boolean) {
//        if (state){
//            translateMethodMap[group] = false
//        }else{
//            translateMethodMap.remove(group)
//        }
//    }
//
//    private fun getTransOnAndOff(groupID: Long): Boolean {
//        return translateMethodMap.containsKey(groupID)
//    }
//
//    fun add(groupID: Long){
//        if (translateMethodMap.containsKey(groupID)){
//            throw Exception("翻译方法表中已存在此群")
//        }
//        translateMethodMap[groupID] = false
//
//    }
//
//    fun getAllMethodGroup(): MutableMap<Long, Boolean> {
//        return translateMethodMap
//    }

    fun autoTranslate(rawMessage: GroupMessage): MessageChain{
        return translate(rawMessage)
    }
//
//    fun isNeedTranslate(groupID: Long): Boolean{
//        return getTransOnAndOff(groupID)
//    }

    private fun translate(rawMessage: GroupMessage): MessageChain{
        // 构造 MessageChain
        val replyMsg = MessageChainBuilder()
        val useBaiduAPI = false
        var arMsg: String
        var isNeedSend = false
        // 写入数据
        rawMessage.message.forEach {

            if ((it.toString().indexOf("mirai:") != -1 ) or (it.toString().indexOf("@") != -1)) {
                // 不启动翻译
                replyMsg.add(it)

            }else{ // 启动翻译
                val text = it.toString()

                arMsg = if (useBaiduAPI){
                    Method().baiduTranslate(text)  //百度翻译
                }else{
                    Method().localTranslate(text) //离线翻译
                }

                if (!arMsg.contains("Error with code")){
                    if (text != arMsg){
                        replyMsg += arMsg
                        isNeedSend = true
                    }
                }

                // debug
                if (useBaiduAPI and debug){
                    println("return from Baidu: $arMsg")
                }
            }
            // if完了
        }
        if (isNeedSend and !replyMsg.equals(MessageChainBuilder().asMessageChain())){
            return replyMsg.asMessageChain() // 返回被翻译过的MsgChain
        }else{
            return MessageChainBuilder().asMessageChain() // 没有翻译过 返回空MsgChain
        }
    }

//    private fun translate(rawMessage: GroupMessage): MessageChain{
//        // 构造 MessageChain
//        val replyMsg = MessageChainBuilder()
//        val useBaiduAPI = getTranslateMethod(rawMessage.group.id)
//        var arMsg: String
//        var isNeedSend = false
//        // 写入数据
//        rawMessage.message.forEach {
//
//            if ((it.toString().indexOf("mirai:") != -1 ) or (it.toString().indexOf("@") != -1)) {
//                // 不启动翻译
//                replyMsg.add(it)
//
//            }else{ // 启动翻译
//                val text = it.toString()
//
//                arMsg = if (useBaiduAPI){
//                    Method().baiduTranslate(text)  //百度翻译
//                }else{
//                    Method().localTranslate(text) //离线翻译
//                }
//
//                if (!arMsg.contains("Error with code")){
//                    if (text != arMsg){
//                        replyMsg += arMsg
//                        isNeedSend = true
//                    }
//                }
//
//                // debug
//                if (useBaiduAPI and debug){
//                    println("return from Baidu: $arMsg")
//                }
//            }
//            // if完了
//        }
//        if (isNeedSend and !replyMsg.equals(MessageChainBuilder().asMessageChain())){
//            return replyMsg.asMessageChain() // 返回被翻译过的MsgChain
//        }else{
//            return MessageChainBuilder().asMessageChain() // 没有翻译过 返回空MsgChain
//        }
//    }
}

class TellTheTimeOnTheDotController{
    fun getNow(): String {
        return SimpleDateFormat("HH:mm").format(Date())
    }
}

class DonaldTrumpController{

    private var DonaldTrumpOnAndOffMap = mutableMapOf<Long, Boolean>()

    fun init(groups: ContactList<Group>) {
        groups.forEach {
            DonaldTrumpOnAndOffMap[it.id] = true
        }
    }

    fun updateTrumpModeOnAndOff(groupID: Long, state: Boolean){
        if (state){
            DonaldTrumpOnAndOffMap[groupID] = true
        }else{
            DonaldTrumpOnAndOffMap.remove(groupID)
        }
    }

    fun getTrumpModeOnAndOff(groupID: Long): Boolean {
        return DonaldTrumpOnAndOffMap[groupID]!!
    }

//    private fun thulac(input:String):String {
//        var tmp = IOUtils.outputToString()
//        Thulac.split(
//            "models/" ,
//            '_' ,
//            null ,
//            false ,
//            false ,
//            false ,
//            IOUtils.inputFromString(input),
//            tmp
//        )
//        return tmp.string
//    }

    private fun textStruct(singleWord:String): String{
        var sentence = arrayListOf<String>()
        sentence.add("我们有全球最好的$singleWord" + "专家\n——特朗普")
        sentence.add("对于$singleWord，没什么需要恐慌的\n——特朗普")
        sentence.add("有些人会说我非常 非常 非常有才，特别是$singleWord"+"方面\n——特朗普")
//        sentence.add(singleWord + "是蛀虫")
        sentence.add("没有人比我特朗普更懂$singleWord")
        return sentence.random()
    }

    fun needSend():Boolean {
//    fun needSend(message: MessageChain):Boolean {
        // TODO
        if ((0..100).random() == 41){
            return true
        }
        return false
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

//    fun TrumpText(input: String): String {
//        for (i in DonaldTrumpController().thulac(input).split(" ")){
//            var each = i.split("_")
//            if (each[1] == "n"){
//                return textStruct(each[0])
//            }
//        }
//        return "没有人比我特朗普....抱歉这个我真不懂"
//    }

    fun TrumpTextWithoutNPL(input: String): String {
        return textStruct(input)
    }
}