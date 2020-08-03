package io.genanik.miraiPlugin.utils

import net.mamoe.mirai.message.data.*

fun MessageChain.removeMsgSource(): MessageChain {
    val tmp = MessageChainBuilder()
    this.forEachContent {
        tmp.add(it)
    }
    return tmp.asMessageChain()
}

fun isHavePicture(rawMessage: MessageChain): Boolean{
    var isHaveImg = false
    rawMessage.forEachContent {
        if (!isHaveImg){
            isHaveImg = it.asMessageChain()
                .firstIsInstanceOrNull<Image>() != null
        }
    }
    return isHaveImg
}

suspend fun getAllPicture(rawMessage: MessageChain): ArrayList<String>{
    val result = ArrayList<String>()
    var tmp = MessageChainBuilder()
    rawMessage.forEachContent {
        tmp.add(it)
        val isImage =
            tmp.asMessageChain()
                .firstIsInstanceOrNull<Image>() != null

        if (isImage){
            // 添加图片url
            result.add((it as Image).queryUrl())
        }
        tmp = MessageChainBuilder()
    }
    return result
}

fun MessageChain.isEqualWithRemoveMsgSource(msgChain: MessageChain): Boolean{
    val newMsgChain = this.removeMsgSource()
    val oldMsgChain = msgChain.removeMsgSource()

    // 首先根据长度判断
    if (newMsgChain.size != oldMsgChain.size){
        return false
    }

    // 根据对比相同索引的内容来判断是否相同
    var new: SingleMessage
    for ( (index, old) in oldMsgChain.withIndex()){
        new = newMsgChain[index]
        if (old != new){
            return false
        }
    }
    return true
}

fun MessageChain.isEqual(msgChain: MessageChain): Boolean{
    // 首先根据长度判断
    if (this.size != msgChain.size){
        return false
    }

    // 根据对比相同索引的内容来判断是否相同
    var new: SingleMessage
    for ( (index, old) in msgChain.withIndex()){
        new = this[index]
        if (old != new){
            return false
        }
    }
    return true
}