package io.genanik.plugin.Settings

import net.mamoe.mirai.message.data.MessageChain

class argList {

    private var argsMap = mutableMapOf<String, (Long) -> MessageChain >()
    private var helpInf = mutableMapOf<String, String>()

    fun regCommand(argStr: String, description: String, function:(Long) -> MessageChain){
        argsMap[argStr] = function
        helpInf[argStr] = description
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

    fun getHelpInformation(): Map<String, String>{
        return helpInf
    }
}