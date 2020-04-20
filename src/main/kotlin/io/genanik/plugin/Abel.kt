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
    var abelPluginController: AbelPluginsManager = AbelPluginsManager()

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
        abelPluginController.regCommand("dumpvars", "发送debug变量") {
            var result = MessageChainBuilder()
            result.add("msgRepeatController: ${msgRepeatController.keys}")
            result.add("debug = $debug")
            return@regCommand result.asMessageChain()
        }
        abelPluginController.regCommand("报时", "发送当前时间") {
            var result = MessageChainBuilder()
            result.add(timeController.getNow())
            return@regCommand result.asMessageChain()
        }
        abelPluginController.regCommand("/help", "展示帮助界面") {
            var result = MessageChainBuilder()
            result.add("嘤嘤嘤嘤嘤嘤嘤嘤嘤\n")
            for (i in abelPluginController.getAllCommands()){
                result.add( "* $i  ${abelPluginController.getCommandDescription()[i]}\n")
            }
            result.add("咱介绍完指令了，嘤嘤嘤\n\n")
            result.add("翻译功能介绍：髪现到群内出现繁体字自动翻译整句消息（\n")
            result.add("复读功能介绍：同一条内容出现两次后自动镜像内容并发送\n")
//            result.add("QQ大火基于Gennaik所编写的Abel插件集和mamoe团队编写的mirai协议库\n")
            result.add("Abel版本: $abelBotVersion\n")
            result.add("Mirai-Core版本: ${MiraiConsole.version}")
            return@regCommand result.asMessageChain()
        }
    }

    override fun onReload(): Boolean {
        onLoad()
        return super.onReload()
    }

    override fun onEnable() {
        super.onEnable()

        logger.info("Plugin loaded!")

        subscribeGroupMessages {
            // 繁体/多语言翻译
            always{
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
                if (msgRepeatController.contains(this.group.id)){
                    if (msgRepeatController[this.group.id]!!.update(this)){

                        reply(msgRepeatController[this.group.id]!!.textBackRepeat(this.message, this.group))
                    }
                }else{
                    msgRepeatController[this.group.id] = MessagesRepeatFunction(this)
                }
            }

            // 川普模式
            always {
                if (msgTrumpController.isAtBot(this.message, this.bot)){
                    var tmp = message.getOrNull(PlainText)
                    if (tmp != null){
                        reply(msgTrumpController.TrumpTextWithoutNPL(
                            tmp.stringValue.replace(" ", "")))
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

    }
}