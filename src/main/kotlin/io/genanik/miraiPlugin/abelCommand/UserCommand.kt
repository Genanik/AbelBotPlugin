package io.genanik.miraiPlugin.abelCommand

import io.genanik.miraiPlugin.AbelPluginMain
import io.genanik.miraiPlugin.Settings.AbelPluginsManager
import io.genanik.miraiPlugin.TimeFunction
import net.mamoe.mirai.message.data.MessageChainBuilder

fun AbelPluginsManager.regHelp(){
    this.regCommand("/help", "展示帮助信息") {
        val result = MessageChainBuilder()
        result.add("你好你好\n\n")
        for (i in this.getAllCommands()) {
            result.add("* $i  ${this.getCommandDescription()[i]}\n")
        }
        result.add("\n咱介绍完指令了，然后。。然后。。。。\n该介绍功能了\n\n")
        for (i in this.getAllFunctions()) {
            result.add("* $i  ${this.getFunctionDescription()[i]}\n")
        }
        result.add(
            "\n其他功能：\n" +
                    "* \"功能名称+打开了嘛\" 获取功能运行状态\n"
        )
        result.add("* /adminHelp 获取管理员帮助信息")
        return@regCommand result.asMessageChain()
    }
}

fun AbelPluginsManager.regGetTime(timeController: TimeFunction){
    this.regCommand("报时", "发送当前时间") {
        val result = MessageChainBuilder()
        result.add(timeController.getNow())
        return@regCommand result.asMessageChain()
    }
}