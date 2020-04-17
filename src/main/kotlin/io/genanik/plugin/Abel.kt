package io.genanik.plugin

import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.MessageChainBuilder
import io.genanik.plugin.Settings.argList
import io.genanik.plugin.Settings.debug
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole


object Abel : PluginBase() {

    val msgRepeatController = MessagesRepeatController()
    val msgTranslateController = MessagesTranslateController()
    val msgTrumpController = DonaldTrumpController()
    val timeController = TellTheTimeOnTheDotController()

    lateinit var awa: ArrayList<String>
    lateinit var argsList: argList

    override fun onLoad() {
        awa = arrayListOf()
        argsList = argList()

    }

    override fun onReload(): Boolean {
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

        // 注册指令
//            argsList.regCommand("切换至百度API") {
//                msgTranslateController.updateMethodMap(it, true)
//                var result = MessageChainBuilder()
//                result.add("不出意外的话......已切换至百度API")
//                return@regCommand result.asMessageChain()
//            }
//            argsList.regCommand("切换至离线API") {
//                msgTranslateController.updateMethodMap(it, false)
//                var result = MessageChainBuilder()
//                result.add("不出意外的话......已切换至离线API")
//                return@regCommand result.asMessageChain()
//            }
        argsList.regCommand("/dumpvars") {
            var result = MessageChainBuilder()
//                result.add("翻译表 = " + msgTranslateController.getAllMethodGroup() + "\n")
//                result.add("msgRepeatControllerMap1 = " + msgRepeatController.getAllMarkedGroup() + "\n")
//                result.add("msgRepeatControllerMap2 = " + msgRepeatController.getLastRepeatMessageMap() + "\n")
            result.add("debug = $debug")
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand("报时") {
            var result = MessageChainBuilder()
            result.add(timeController.getNow())
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand("/help") {
            var result = MessageChainBuilder()
            result.add("All Commands: " + argsList.getAllCommands())
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand("切换至川普模式"){
            msgTrumpController.updateTrumpModeOnAndOff(it , true)
//                msgRepeatController.updateRepeatOnAndOff(miraiBot.getGroup(it),false)
//                msgTranslateController.updateTransOnAndOff(miraiBot.getGroup(it),false)
            var result = MessageChainBuilder()
            result.add("我特朗普要让让复读机再次伟大！（已关闭复读翻译功能")
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand("退出川普模式"){
            msgTrumpController.updateTrumpModeOnAndOff(it, false)
//                msgRepeatController.updateRepeatOnAndOff(miraiBot.getGroup(it),true)
//                msgTranslateController.updateTransOnAndOff(miraiBot.getGroup(it),true)
            var result = MessageChainBuilder()
            result.add("已退出特朗普模式并重新开启复读翻译功能")
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand( "关闭复读"){
            msgRepeatController.updateRepeatOnAndOff(it, false)
            var result = MessageChainBuilder()
            result.add("已关闭在此群的复读功能")
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand( "开启复读"){
            msgRepeatController.updateRepeatOnAndOff(it, true)
            var result = MessageChainBuilder()
            result.add("已重新开启在此群的复读功能")
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand( "关闭翻译"){
//                msgTranslateController.updateTransOnAndOff(miraiBot.getGroup(it), false)
            var result = MessageChainBuilder()
            result.add("已关闭在此群的翻译功能")
            return@regCommand result.asMessageChain()
        }
        argsList.regCommand( "开启翻译"){
//                msgTranslateController.updateTransOnAndOff(miraiBot.getGroup(it), true)
            var result = MessageChainBuilder()
            result.add("已重新开启在此群的翻译功能")
            return@regCommand result.asMessageChain()
        }
        return true
    }

    override fun onEnable() {
        super.onEnable()

        logger.info("Plugin loaded!")

        subscribeGroupMessages {
            // 繁体/多语言翻译
            always{
//                if (msgTranslateController.isNeedTranslate(group.id)){
//                    var tmp = msgTranslateController.autoTranslate(this)
//                    if ((tmp.toString() != "") and (this.sender.id != 2704749081L)){
//                        reply(tmp)
//                    }
//                }
                var tmp = msgTranslateController.autoTranslate(this)
                if ((tmp.toString() != "") and (this.sender.id != 2704749081L)){
                    reply(tmp)
                }
            }

            // 喵喵喵
            contains("喵喵喵喵"){
                reply(awa[(0..awa.size).shuffled().last()])
            }

            // 复读
            always {
                if (msgRepeatController.isNowRepeat(this)){
                    reply(msgRepeatController.textBackRepeat(message, group))
                }
            }

            // 川普模式
            always {
                if (msgTrumpController.isAtBot(this.message, this.bot)){
                    reply(msgTrumpController.TrumpTextWithoutNPL(message.toString()))
                }
            }
        }

        // 机器人指令绑定
        subscribeGroupMessages {
            for (i in argsList.getAllCommands()){
                case(i) {
                    reply( argsList.transferCommand(i)(this.group.id))
                }
            }
        }

    }
}