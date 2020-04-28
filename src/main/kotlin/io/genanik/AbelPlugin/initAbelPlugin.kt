package io.genanik.AbelPlugin

import io.genanik.AbelPlugin.Settings.AbelPluginsManager
import io.genanik.AbelPlugin.Settings.AdminPluginsManager
import io.genanik.AbelPlugin.Settings.UserPluginManager
import io.genanik.AbelPlugin.Settings.abelBotVersion
import io.genanik.plugin.TimeFunction
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.MiraiLogger


fun initAbelUserCommand(logger: MiraiLogger, abel: UserPluginManager){
    logger.info("开始注册Abel指令")

    val timeController = TimeFunction()

    abel.regCommand("/help", "展示帮助信息") {
        val result = MessageChainBuilder()
        result.add("嘤嘤嘤嘤嘤嘤嘤嘤嘤\n\n")
        for (i in abel.getAllCommands()){
            result.add( "* $i  ${abel.getCommandDescription()[i]}\n")
        }
        result.add("\n咱介绍完指令了，嘤嘤嘤，该介绍功能了\n\n")
        for (i in abel.getAllFunctions()){
            result.add( "* $i  ${abel.getFunctionDescription()[i]}\n")
        }
        result.add("\n其他功能：\n* \"功能名称+打开了嘛\" 获取功能运行状态\n")
        result.add("* /adminHelp 获取管理员帮助信息\n")
        result.add("\nAbel版本: $abelBotVersion\n")
        result.add("Mirai-Core版本: ${MiraiConsole.version}")
        return@regCommand result.asMessageChain()
    }

    abel.regCommand("报时", "发送当前时间") {
        val result = MessageChainBuilder()
        result.add(timeController.getNow())
        return@regCommand result.asMessageChain()
    }
}

fun initAbelAdminCommand(logger: MiraiLogger, abel: AdminPluginsManager){
    logger.info("开始注册Abel-Admin指令")

    abel.regCommand("/adminHelp", "展示帮助信息") {
        val result = MessageChainBuilder()
        result.add("嘤嘤嘤嘤嘤嘤嘤嘤嘤\n\n")
        for (i in abel.getAllCommands()){
            result.add( "* $i  ${abel.getCommandDescription()[i]}\n")
        }
        result.add("\n咱介绍完指令了，嘤嘤嘤，该介绍功能了\n\n")
        for (i in abel.getAllFunctions()){
            result.add( "* $i  ${abel.getFunctionDescription()[i]}\n")
        }
        result.add("\n其他功能：\n* \"功能名称+打开了嘛\" 获取功能运行状态\n")
        result.add("* /adminHelp 获取管理员帮助信息\n")
        result.add("\nAbel版本: $abelBotVersion\n")
        result.add("Mirai-Core版本: ${MiraiConsole.version}")
        return@regCommand result.asMessageChain()
    }
}