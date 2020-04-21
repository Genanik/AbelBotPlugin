package io.genanik.plugin

import io.genanik.plugin.Settings.abelBotVersion
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.MessageChainBuilder
import io.genanik.plugin.Settings.AbelPluginsManager
import io.genanik.plugin.Settings.debug
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.message.data.PlainText

object Abel: PluginBase() {

    val msgRepeatController = mutableMapOf<Long, MessagesRepeatFunction>()
    val msgTranslateController = MessagesTranslateFunction()
    val msgTrumpController = DonaldTrumpFunction()
    val timeController = TellTheTimeFunction()

    lateinit var awa: ArrayList<String>
    var abelPluginController: AbelPluginsManager = AbelPluginsManager(logger)

    override fun onLoad() {
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
        // 注册Abel指令
        logger.info("开始注册Abel指令")
        abelPluginController.regCommand("/help", "展示帮助界面") {
            val result = MessageChainBuilder()
            result.add("嘤嘤嘤嘤嘤嘤嘤嘤嘤\n\n")
            for (i in abelPluginController.getAllCommands()){
                result.add( "* $i  ${abelPluginController.getCommandDescription()[i]}\n")
            }
            result.add("\n咱介绍完指令了，嘤嘤嘤，该介绍功能了\n\n")
            for (i in abelPluginController.getAllFunctions()){
                result.add( "* $i  ${abelPluginController.getFunctionDescription()[i]}\n")
            }
            result.add("\nAbel版本: $abelBotVersion\n")
            result.add("Mirai-Core版本: ${MiraiConsole.version}")
            return@regCommand result.asMessageChain()
        }
        abelPluginController.regCommand("dumpvars", "发送debug变量") {
            val result = MessageChainBuilder()
            result.add("msgRepeatController: ${msgRepeatController.keys}")
            result.add("debug = $debug")
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
                    val tmp = msgTranslateController.autoTranslate(this)
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
                            reply(msgTrumpController.TrumpTextWithoutNPL(
                                tmp.stringValue.replace(" ", "")))
                        }
                    }
                }
            }
        }

        // Abel指令绑定
        subscribeGroupMessages {
            for (i in abelPluginController.getAllCommands()){
                case(i) {
                    reply( abelPluginController.transferCommand(i)(this.group.id))
                }
            }
        }
        // Abel功能绑定
        subscribeGroupMessages {
            for (i in abelPluginController.getAllFunctions()){
                case("关闭$i") {
                    abelPluginController.disableFunc(i, this.group.id)
                    reply("不出意外的话。。咱已经关掉${i}了")
                }
                case("开启$i") {
                    abelPluginController.enableFunc(i, this.group.id)
                    reply("不出意外的话。。咱打开${i}了")
                }
            }
        }
    }
}