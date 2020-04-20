package io.genanik.plugin.Settings

import net.mamoe.mirai.message.data.MessageChain
import sun.security.ec.point.ProjectivePoint

class AbelPluginsManager {

    /**
     * AbelPluginsManager有两个控制器
     * 一个是Command控制器 一个是Function控制器
     * Command  由特定字符串触发
     * Function 需要自己写一个subscribeGroupMessages
     * - Function一个是Function控制器现阶段仅可保存开关状态与功能描述 TODO 未来考虑构建DSL
     */


    private var argsMap = mutableMapOf<String, (Long) -> MessageChain >()
    private var commandHelpInf = mutableMapOf<String, String>()

    /**
     * 注册指令
     * argStr: 触发指令所用关键字
     * description: 显示在/help
     * function: 触发指令时执行的函数
     * - function -> Long:  触发指令的群号
     * - function <- MessageChain: 触发指令后机器人的回复内容
     */
    fun regCommand(argStr: String, description: String, function:(Long) -> MessageChain){
        argsMap[argStr] = function
        commandHelpInf[argStr] = description
    }

    /**
     * 文本消息翻译为指令
     */
    fun transferCommand(argStr:String): ((Long) -> MessageChain) {
        return argsMap[argStr]!!
    }

    /**
     * 当前注册指令中是否包含名为argStr的指令
     */
    fun contains(argStr: String): Boolean {
        return argsMap.containsKey(argStr)
    }

    /**
     * 返回所有已注册的指令
     */
    fun getAllCommands(): MutableSet<String> {
        return argsMap.keys
    }

    /**
     * 获取Map 指令名->指令介绍
     */
    fun getCommandDescription(): Map<String, String>{
        return commandHelpInf
    }



    private var functionMap = mutableMapOf<String, MutableList<Long>>() //群号存在这里面就是关闭了
    private var functionHelpInf = mutableMapOf<String, String>()

    /**
     * 注册功能
     * name: 功能名称
     * description: 显示在/help
     * 默认开启
     */
    fun regFunction(name: String, description: String){
        // newFuncSwitchList 储存已关闭该功能的群号
        var newFuncSwitchList = mutableListOf<Long>()
        functionMap[name] = newFuncSwitchList
        functionHelpInf[name] = description
    }

    /**
     * 获取function当前是否被开启
     */
    fun getStatus(name: String, groupID: Long): Boolean {
        return !functionMap[name]!!.contains(groupID)
    }

    /**
     * 关闭function
     */
    fun disableFunc(name: String, groupID: Long){
        var newFuncSwitchList = functionMap[name]!!
        newFuncSwitchList.add(groupID)
        functionMap[name] = newFuncSwitchList
    }

    /**
     * 开启function
     */
    fun enableFunc(name: String, groupID: Long){
        var newFuncSwitchList = functionMap[name]!!
        newFuncSwitchList.remove(groupID)
        functionMap[name] = newFuncSwitchList
    }

}