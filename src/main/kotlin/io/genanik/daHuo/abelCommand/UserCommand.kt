package io.genanik.daHuo.abelCommand

import io.genanik.daHuo.plugins.Time
import io.genanik.daHuo.abel.AbelPlugins
import net.mamoe.mirai.message.data.MessageChainBuilder

fun AbelPlugins.regHelp(){
    this.regCommand("/help", "展示帮助信息") {
        val result = MessageChainBuilder()
        result.add("你好你好\n")
        for (i in this.getAllCommands()) {
            result.add("* $i  ${this.getCommandDescription()[i]}\n")
        }
        result.add("\n咱介绍完指令了\n该介绍功能了\n\n")
        for (i in this.getAllFunctions()) {
            result.add("* $i  ${this.getFunctionDescription()[i]}\n")
        }
        result.add(
            "\n其他功能：\n" +
                    "* \"功能名称+打开了嘛\" 获取功能运行状态\n"
        )
        result.add("* /adminHelp 获取管理员帮助信息\n")
        result.add("\n指令触发的话只需要发一条与指令一模一样的消息就可以了\n")
        result.add("功能触发条件..emm写在功能描述里了")
        return@regCommand result.asMessageChain()
    }
}

fun AbelPlugins.regGetTime(timeController: Time){
    this.regCommand("报时", "发送当前时间") {
        val result = MessageChainBuilder()
        result.add(timeController.getNow())
        return@regCommand result.asMessageChain()
    }
}