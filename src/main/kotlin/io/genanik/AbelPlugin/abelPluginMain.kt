package io.genanik.AbelPlugin

import io.genanik.AbelPlugin.Settings.AbelPluginsManager
import io.genanik.AbelPlugin.Settings.UserPluginManager
import io.genanik.AbelPlugin.Settings.abelBotVersion
import io.genanik.plugin.DonaldTrumpFunction
import io.genanik.plugin.MessagesRepeatFunction
import io.genanik.plugin.MessagesTranslateFunction
import io.genanik.plugin.TimeFunction
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText

object abelPluginMain : PluginBase() {

    val msgRepeatController = mutableMapOf<Long, MessagesRepeatFunction>()
    val msgTranslateController = MessagesTranslateFunction()
    val msgTrumpController = DonaldTrumpFunction()


    val userPluginController = UserPluginManager(logger)
    val adminPluginController = AdminPluginManager(logger)

    override fun onLoad() {
        super.onLoad()

        // 注册Mirai指令
        // 暂无
        // 注册Abel管理员指令
        initAbelAdminCommand(logger, adminPluginController)
        // 注册Abel用户指令
        initAbelUserCommand(logger, userPluginController)

        // 注册Abel功能
        userPluginController.regFunction("翻译", "自动翻译包含繁体的消息")
        userPluginController.regFunction("复读", "同一条消息出现两次后，Abel机器人自动跟读")
        userPluginController.regFunction("川普", "@Abel机器人并加上一个关键词，自动发送名人名言")
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
                            reply(msgTrumpController.textStruct(
                                tmp.stringValue.replace(" ", "")))
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
                    if (abelPluginController.adminGetStatus(i, this.group.id)){
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
                    if (abelPluginController.adminGetStatus(i, this.group.id)){
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
            for (i in abelPluginController.adminGetAllFunctions() ){
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