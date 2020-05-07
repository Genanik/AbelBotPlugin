package io.genanik.miraiPlugin

import io.genanik.miraiPlugin.Settings.AbelPluginsManager
import io.genanik.miraiPlugin.Settings.abelBotVersion
import io.genanik.miraiPlugin.Settings.debug
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.Config
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText

object AbelPluginMain : PluginBase() {

    val msgRepeatController = mutableMapOf<Long, MessagesRepeatFunction>()
    val msgTranslateController = MessagesTranslateFunction()
    val msgTrumpController = DonaldTrumpFunction()
    val timeController = TimeFunction()

    lateinit var awa: ArrayList<String>

    lateinit var abelPluginController: AbelPluginsManager
    lateinit var settings: Config

    override fun onLoad() {
        super.onLoad()
        settings = getResourcesConfig("abelPluginController.yml")
        abelPluginController = AbelPluginsManager(logger, settings)

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
            result.add("AbelPluginController: " +
                    "${abelPluginController.getAllCommands()}\n" +
                    "${abelPluginController.getAllFunctions()}\n")
            result.add("Debug: $debug\n")
            result.add("AbelVersion: $abelBotVersion\n")
            result.add("MiraiVersion: $${MiraiConsole.version}")
            return@regAdminCommand result.asMessageChain()
        }
        abelPluginController.regAdminCommand("/adminHelp"){
            val result = MessageChainBuilder()
            result.add("禁用{功能}\n")
            result.add("启用{功能}\n")
            result.add(abelPluginController.adminGetAllCommands().toString() + "\n")
            result.add(abelPluginController.adminGetAllFunctions().toString())
            return@regAdminCommand result.asMessageChain()
        }
        // 注册Abel指令
        logger.info("开始注册Abel指令")
        abelPluginController.regCommand("/help", "展示帮助信息") {
            val result = MessageChainBuilder()
            result.add("嘤嘤嘤嘤嘤嘤嘤嘤嘤\n\n")
            for (i in abelPluginController.getAllCommands()){
                result.add( "* $i  ${abelPluginController.getCommandDescription()[i]}\n")
            }
            result.add("\n咱介绍完指令了，嘤嘤嘤，该介绍功能了\n\n")
            for (i in abelPluginController.getAllFunctions()){
                result.add( "* $i  ${abelPluginController.getFunctionDescription()[i]}\n")
            }
            result.add("\n其他功能：\n* \"功能名称+打开了嘛\" 获取功能运行状态\n")
            result.add("* /adminHelp 获取管理员帮助信息\n")
//            result.add("\nAbel版本: $abelBotVersion\n")
//            result.add("Mirai-Core版本: ${MiraiConsole.version}")
            return@regCommand result.asMessageChain()
        }
        abelPluginController.regCommand("报时", "发送当前时间") {
            val result = MessageChainBuilder()
            result.add(timeController.getNow())
            return@regCommand result.asMessageChain()
        }
        abelPluginController.regCommand("喵喵喵喵", "awa"){
            val result = MessageChainBuilder()
            result.add(awa[(0..awa.size).shuffled().last()])
            return@regCommand result.asMessageChain()
        }

        // 注册Abel管理员功能
        abelPluginController.adminRegFunction("翻译")
        abelPluginController.adminRegFunction("复读")
        abelPluginController.adminRegFunction("川普")

        // 注册Abel功能
        abelPluginController.regFunction("翻译", "自动翻译包含繁体的消息")
        abelPluginController.regFunction("复读", "同一条消息出现两次后，Abel机器人自动跟读")
        abelPluginController.regFunction("川普", "@Abel机器人并加上一个关键词，自动发送名人名言")
    }

    override fun onEnable() {
        super.onEnable()
        logger.info("Plugin loaded!")

        /**
         * 订阅Abel功能实现 未来改用DSL内置到Abel插件框架
         */
        subscribeGroupMessages {
            // 翻译
            always{
                if (abelPluginController.getStatus("翻译", this.group.id)){
                    val tmp = msgTranslateController.translate(this)
                    if ((tmp.toString() != "") and (this.sender.id != 2704749081L)){
                        reply(tmp)
                    }
                }
            }

            // 复读
            always {
                if (abelPluginController.getStatus("复读", this.group.id)){
                    if (msgRepeatController.contains(this.group.id)){
                        if (msgRepeatController[this.group.id]!!.update(this)){
                            reply(msgRepeatController[this.group.id]!!.textBackRepeat(this.message, this.group))
                        }
                    }else{
                        msgRepeatController[this.group.id] = MessagesRepeatFunction(this)
                    }
                }
            }

            // 川普
            always {
                if (abelPluginController.getStatus("川普", this.group.id)){
                    if (msgTrumpController.isAtBot(this.message, this.bot)){
                        val tmp = message.getOrNull(PlainText)
                        if (tmp != null){
                            val keyWord = tmp.stringValue.replace(" ", "")
                            if (keyWord != ""){
                                reply(msgTrumpController.TrumpTextWithoutNPL(keyWord))
                            }
                        }
                    }
                }
            }
        }

        // Abel指令绑定
        subscribeGroupMessages {
            for (i in abelPluginController.adminGetAllCommands()){
                case(i) {
                    reply( abelPluginController.adminTransferCommand(i)(this.group.id))
                }
            }
            for (i in abelPluginController.getAllCommands()){
                case(i) {
                    reply( abelPluginController.transferCommand(i)(this.group.id))
                }
            }
        }
        // Abel功能绑定
        subscribeGroupMessages {
            // 用户组
            for (i in abelPluginController.getAllFunctions()){
                // 操作
                case("关闭$i") {
                    if (!abelPluginController.adminGetStatus(i, this.group.id)){
                        if (abelPluginController.getStatus(i, this.group.id)){
                            abelPluginController.disableFunc(i, this.group.id)
                            reply("不出意外的话。。咱关掉${i}了")
                        }else{
                            reply("这个功能已经被关掉了呢_(:з」∠)_不用再关一次了\n" +
                                    "推荐使用\"功能名称+打开了嘛\"获取功能运行状态")
                        }
                    }
                }
                case("开启$i") {
                    if (!abelPluginController.adminGetStatus(i, this.group.id)){
                        if (!abelPluginController.getStatus(i,this.group.id)){
                            abelPluginController.enableFunc(i, this.group.id)
                            reply("不出意外的话。。咱打开${i}了")
                        }else{
                            reply("(｡･ω･)ﾉﾞ${i}\n这个已经打开了哦，不用再开一次啦\n" +
                                    "推荐使用\"功能名称+打开了嘛\"获取功能运行状态")
                        }
                    }
                }

                // 查询
                case("${i}打开了嘛") {
                    if (abelPluginController.getStatus(i,this.group.id)){
                        reply("没有ヽ(･ω･｡)ﾉ ")
                    }else{
                        reply("开啦(′▽`〃)")
                    }
                }
            }

            // 管理员
            for (i in abelPluginController.adminGetAllFunctions()){
                // 操作
                case("禁用$i") {
                    if (abelPluginController.isAdmin(this.sender.id)){

                        abelPluginController.adminDisableFunc(i, this.group.id)
                        abelPluginController.disableFunc(i, this.group.id)
                        reply("群: ${this.group.id}\n已禁用功能: $i")
                    }
                }
                case("启用$i") {
                    if (abelPluginController.isAdmin(this.sender.id)){

                        abelPluginController.adminEnableFunc(i, this.group.id)
                        abelPluginController.enableFunc(i, this.group.id)
                        reply("群: ${this.group.id}\n已启用功能: $i")
                    }
                }
            }
        }
    }
}