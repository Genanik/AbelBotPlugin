package io.genanik.daHuo.abelCommand

import io.genanik.daHuo.abel.AbelPlugins
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.message.data.MessageChainBuilder
import java.io.File

fun AbelPlugins.regDumpvars(){
    this.regAdminCommand("dumpvars") {
        val result = MessageChainBuilder()
        result.add("保留指令")
        return@regAdminCommand result.asMessageChain()
    }
}


fun AbelPlugins.regAdminHelp(){
    this.regAdminCommand("/adminHelp") {
        val result = MessageChainBuilder()
        result.add("启用{功能}\n")
        result.add("禁用{功能}\n")
        result.add("切换{功能}\n")
        result.add(this.adminGetAllCommands().toString() + "\n")
        result.add(this.adminGetAllFunctions().toString() + "\n")
        result.add(
            "AbelPluginController: " +
                    "${this.getAllCommands()}\n" +
                    "${this.getAllFunctions()}\n"
        )
        result.add("AbelVersion: ${Versions.DaHuo.version}\n")
        result.add("ABelFrameworkVersion: ${Versions.AbelFramework.version}\n")
        result.add("JavaVersion: ${System.getProperty("java.version")}\n")
        result.add(
            "MiraiCoreVersion: ${File(Bot.javaClass.protectionDomain.codeSource.location.file).name.replace(
                ".jar",
                ""
            )}\n"
        )
        result.add("MiraiConsleVersion: ${MiraiConsole.version} - ${MiraiConsole.build}")
        return@regAdminCommand result.asMessageChain()
    }
}

fun AbelPlugins.regAdminFunctions(){
    this.adminRegFunction("翻译")
    this.adminRegFunction("复读")
    this.adminRegFunction("川普")
    this.adminRegFunction("倒转GIF")
    this.adminRegFunction("图片缩放")
    this.adminRegFunction("bilibili")
    this.adminRegFunction("三贱客")
}