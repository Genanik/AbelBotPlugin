package io.genanik.miraiPlugin.utils

import net.mamoe.mirai.message.data.*

fun removeMessageSource(message: MessageChain): MessageChain {
    val tmp = MessageChainBuilder()
    message.forEachContent {
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