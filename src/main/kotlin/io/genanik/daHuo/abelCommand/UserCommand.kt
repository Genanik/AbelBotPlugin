package io.genanik.daHuo.abelCommand

import io.genanik.daHuo.plugins.Time
import io.genanik.daHuo.abel.AbelPlugins
import net.mamoe.mirai.message.data.MessageChainBuilder

fun AbelPlugins.regHelp(){
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

fun AbelPlugins.regGetTime(timeController: Time){
    this.regCommand("报时", "发送当前时间") {
        val result = MessageChainBuilder()
        result.add(timeController.getNow())
        return@regCommand result.asMessageChain()
    }
}

fun AbelPlugins.regFunctions(){
    this.regFunction("翻译", "自动翻译包含繁体的消息")
    this.regFunction("复读", "同一条消息出现两次后，Abel机器人自动跟读")
    this.regFunction("川普", "@Abel机器人并加上一个关键词，自动发送\"名人名言\"")
    this.regFunction("倒转GIF", "@Abel机器人并加上一个或多个GIF，可以倒叙一个或多个GIF")
    this.regFunction("图片缩放", "@Abel机器人并加上\"放大\"或\"缩小\"一个或多个静态图，可以缩放静态图")
    this.regFunction("bilibili", "发现b站视频链接自动生成预览信息")
}