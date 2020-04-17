package io.genanik.plugin.Settings

import net.mamoe.mirai.message.data.MessageChain

class argList {

    fun argList(){}

    private var argsMap = mutableMapOf<String, (Long) -> MessageChain >()

    fun regCommand(argStr:String, function:(Long) -> MessageChain){
        argsMap[argStr] = function
    }

    fun transferCommand(argStr:String): ((Long) -> MessageChain) {
        return argsMap[argStr]!!
    }

    fun contains(argStr: String): Boolean {
        return argsMap.containsKey(argStr)
    }

    fun getAllCommands(): MutableSet<String> {
        return argsMap.keys
    }
}