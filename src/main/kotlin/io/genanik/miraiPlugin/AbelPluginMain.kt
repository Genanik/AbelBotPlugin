package io.genanik.miraiPlugin

import io.genanik.miraiPlugin.Settings.AbelPluginsManager
import io.genanik.miraiPlugin.Settings.abelBotVersion
import io.genanik.miraiPlugin.Settings.debug
import io.genanik.miraiPlugin.uttil.getAllPicture
import io.genanik.miraiPlugin.uttil.isHavePicture
import io.genanik.miraiPlugin.uttil.reverseImage
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.*
import java.io.File

object AbelPluginMain : PluginBase() {

    val msgRepeatController = mutableMapOf<Long, MessagesRepeatFunction>()
    val msgTranslateController = MessagesTranslateFunction()
    val msgTrumpController = DonaldTrumpFunction()
    val timeController = TimeFunction()

    lateinit var awa: ArrayList<String>

    lateinit var abelPluginController: AbelPluginsManager

    override fun onLoad() {
        super.onLoad()
        abelPluginController = AbelPluginsManager(logger)

        awa = arrayListOf()

        // 添加awa字符
        awa.add("w(ﾟДﾟ)w")
        awa.add("ヽ(✿ﾟ▽ﾟ)ノ")
        awa.add("Σ( ° △ °|||)︴")
        awa.add("φ(≧ω≦*)♪")
        awa.add("(°ー°〃)")
        awa.add("喵喵喵？")
        awa.add("喵喵！")
        awa.add("(≧▽≦)喵？")
        awa.add("\"˚∆˚\"")
        awa.add("Orz")
        awa.add("诶嘿，我不叫(≧ω≦)))")

        // 注册Mirai指令
        // 暂无
        // 注册Abel管理员指令
        abelPluginController.regAdminCommand("dumpvars") {
            val result = MessageChainBuilder()
            result.add("保留指令")
            return@regAdminCommand result.asMessageChain()
        }
        abelPluginController.regAdminCommand("/adminHelp") {
            val result = MessageChainBuilder()
            result.add("启用{功能}\n")
            result.add("禁用{功能}\n")
            result.add("切换{功能}\n")
            result.add(abelPluginController.adminGetAllCommands().toString() + "\n")
            result.add(abelPluginController.adminGetAllFunctions().toString() + "\n")
            result.add(
                "AbelPluginController: " +
                        "${abelPluginController.getAllCommands()}\n" +
                        "${abelPluginController.getAllFunctions()}\n"
            )
            result.add("Debug: $debug\n")
            result.add("AbelVersion: $abelBotVersion\n")
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
        // 注册Abel指令
        logger.info("开始注册Abel指令")
        abelPluginController.regCommand("/help", "展示帮助信息") {
            val result = MessageChainBuilder()
            result.add("嘤嘤嘤嘤嘤嘤嘤嘤嘤\n\n")
            for (i in abelPluginController.getAllCommands()) {
                result.add("* $i  ${abelPluginController.getCommandDescription()[i]}\n")
            }
            result.add("\n咱介绍完指令了，嘤嘤嘤，该介绍功能了\n\n")
            for (i in abelPluginController.getAllFunctions()) {
                result.add("* $i  ${abelPluginController.getFunctionDescription()[i]}\n")
            }
            result.add(
                "\n其他功能：\n" +
                        "* \"功能名称+打开了嘛\" 获取功能运行状态\n"
            )
            result.add("* /adminHelp 获取管理员帮助信息")
            return@regCommand result.asMessageChain()
        }
        abelPluginController.regCommand("报时", "发送当前时间") {
            val result = MessageChainBuilder()
            result.add(timeController.getNow())
            return@regCommand result.asMessageChain()
        }
        abelPluginController.regCommand("喵喵喵喵", "awa") {
            val result = MessageChainBuilder()
            result.add(awa[(0..awa.size).shuffled().last()])
            return@regCommand result.asMessageChain()
        }

        // 注册Abel管理员功能
        abelPluginController.adminRegFunction("翻译")
        abelPluginController.adminRegFunction("复读")
        abelPluginController.adminRegFunction("川普")
        abelPluginController.adminRegFunction("倒转GIF")

        // 注册Abel功能
        abelPluginController.regFunction("翻译", "自动翻译包含繁体的消息")
        abelPluginController.regFunction("复读", "同一条消息出现两次后，Abel机器人自动跟读")
        abelPluginController.regFunction("川普", "@Abel机器人并加上一个关键词，自动发送\"名人名言\"")
        abelPluginController.regFunction("倒转GIF", "@Abel机器人并加上一个或多个GIF，可以倒叙一个或多个GIF")
    }

    override fun onEnable() {
        super.onEnable()
        logger.info("Plugin loaded!")

        /**
         * 订阅Abel功能实现 未来改用DSL内置到Abel插件框架
         */
        subscribeGroupMessages {
            // 翻译
            always {
                if (!abelPluginController.getStatus("翻译", this.group.id)) { // 默认不开启
                    val tmp = msgTranslateController.translate(this)
                    if ((tmp.toString() != "")) {
                        reply(tmp)
                    }
                }
            }

            // 复读
            always {
                if (abelPluginController.getStatus("复读", this.group.id)) {
                    if (msgRepeatController.contains(this.group.id)) {
                        // 更新msgRepeat内容
                        if (msgRepeatController[this.group.id]!!.update(this)) {
                            reply(msgRepeatController[this.group.id]!!.textBackRepeat(this.message, this.group))
                        }
                    } else {
                        // 为本群创建一个msgRepeat
                        msgRepeatController[this.group.id] = MessagesRepeatFunction(this)
                    }
                }
            }

            // 川普
            atBot {
                if (abelPluginController.getStatus("川普", this.group.id)) {
                    if (!isHavePicture(message)) {
                        val tmp = message.firstIsInstanceOrNull<PlainText>()
                        if (tmp != null) {
                            val keyWord = tmp.content.replace(" ", "")
                            if (keyWord != "") {
                                reply(msgTrumpController.TrumpTextWithoutNPL(keyWord))
                            }
                        }
                    }
                }
            }

            // 倒转GIF
            atBot {
                if (abelPluginController.getStatus("倒转GIF", this.group.id)) {
                    if (isHavePicture(message)) {
                        val newMsg = MessageChainBuilder()
                        val allPic = getAllPicture(message)
                        allPic.forEach { picUrl ->
                            newMsg.add(reverseImage(picUrl, group))
                        }
                        reply(newMsg.asMessageChain())
                    }
                }
            }
        }

        // Abel指令绑定
        subscribeGroupMessages {
            for (i in abelPluginController.adminGetAllCommands()) {
                case(i) {
                    reply(abelPluginController.adminTransferCommand(i)(this.group.id))
                }
            }
            for (i in abelPluginController.getAllCommands()) {
                case(i) {
                    reply(abelPluginController.transferCommand(i)(this.group.id))
                }
            }
        }
        // Abel功能绑定
        subscribeGroupMessages {
            // 用户组
            for (i in abelPluginController.getAllFunctions()) {
                // 操作
                case("关闭$i") {
                    if (!abelPluginController.adminGetStatus(i, this.group.id)) {
                        if (abelPluginController.getStatus(i, this.group.id)) {
                            abelPluginController.disableFunc(i, this.group.id)
                            reply("不出意外的话。。咱关掉${i}了")
                        } else {
                            reply(
                                "这个功能已经被关掉了呢_(:з」∠)_不用再关一次了\n" +
                                        "推荐使用\"功能名称+打开了嘛\"获取功能运行状态"
                            )
                        }
                    }
                }
                case("开启$i") {
                    if (!abelPluginController.adminGetStatus(i, this.group.id)) {
                        if (!abelPluginController.getStatus(i, this.group.id)) {
                            abelPluginController.enableFunc(i, this.group.id)
                            reply("不出意外的话。。咱打开${i}了")
                        } else {
                            reply(
                                "(｡･ω･)ﾉﾞ${i}\n这个已经打开了哦，不用再开一次啦\n" +
                                        "推荐使用\"功能名称+打开了嘛\"获取功能运行状态"
                            )
                        }
                    }
                }

                case("切换$i") {
                    if (!abelPluginController.adminGetStatus(i, this.group.id)) {
                        if (!abelPluginController.getStatus(i, this.group.id)) {
                            abelPluginController.enableFunc(i, this.group.id)
                            reply("不出意外的话。。咱打开${i}了")
                        } else {
                            abelPluginController.disableFunc(i, this.group.id)
                            reply("不出意外的话。。咱关掉${i}了")
                        }
                    }
                }

                // 查询
                case("${i}打开了嘛") {
                    var status = abelPluginController.getStatus(i, this.group.id)
                    if (i == "翻译") {
                        status = !status
                    }
                    if (status) {
                        reply("开啦(′▽`〃)")
                    } else {
                        reply("没有ヽ(･ω･｡)ﾉ ")
                    }
                }
            }

            // 管理员
            for (i in abelPluginController.adminGetAllFunctions()) {
                // 操作
                case("禁用$i") {
                    if (abelPluginController.isAdmin(this.sender.id)) {

                        abelPluginController.adminDisableFunc(i, this.group.id)
                        abelPluginController.disableFunc(i, this.group.id)
                        reply("群: ${this.group.id}\n已禁用功能: $i")
                    }
                }
                case("启用$i") {
                    if (abelPluginController.isAdmin(this.sender.id)) {

                        abelPluginController.adminEnableFunc(i, this.group.id)
                        abelPluginController.enableFunc(i, this.group.id)
                        reply("群: ${this.group.id}\n已启用功能: $i")
                    }
                }
            }
        }

        // 临时消息
        subscribeTempMessages {
            always {
                reply("emm抱歉。。暂不支持临时会话，但是可以通过邀请至群使用（加好友自动通过验证），群内/help查看帮助")
            }
        }

        // 好友消息
        subscribeFriendMessages {
            always {
                reply("emm抱歉。。暂不支持私聊，但是可以通过邀请至群使用（加好友自动通过验证），群内/help查看帮助")
            }
        }

        subscribeAlways<NewFriendRequestEvent> {
            logger.info("成功添加新好友, eventID:$eventId message:$message")
            accept()
        }

    }

}