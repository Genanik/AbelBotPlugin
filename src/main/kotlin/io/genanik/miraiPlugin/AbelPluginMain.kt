package io.genanik.miraiPlugin

import io.genanik.miraiPlugin.Settings.AbelPluginsManager
import io.genanik.miraiPlugin.abelCommand.*
import io.genanik.miraiPlugin.plugins.*
import io.genanik.miraiPlugin.utils.*
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.*

object AbelPluginMain : PluginBase() {

    // 为每个Abel插件创建对象
    private val msgRepeatController = mutableMapOf<Long, MessagesRepeat>()
    private val msgTranslateController = MessagesTranslate()
    private val msgTrumpController = DonaldTrump()
    private val msgReverseGIF = ReverseGIF()
    private val msgImageResize = ImageResize()
    private val timeController = Time()

    private var abelPluginController = AbelPluginsManager(logger)

    override fun onLoad() {
        super.onLoad()
        // 创建AbelPic文件夹
        createAbelPicFolder()

        // 注册Abel管理员指令
        logger.info("注册Abel管理员指令")
        abelPluginController.regDumpvars()
        abelPluginController.regAdminHelp()

        // 注册Abel指令
        logger.info("注册Abel指令")
        abelPluginController.regHelp()
        abelPluginController.regGetTime(timeController)

        // 注册Abel管理员功能
        logger.info("注册Abel管理员功能")
        abelPluginController.regAdminFunctions()

        // 注册Abel功能
        logger.info("注册Abel功能")
        abelPluginController.regFunctions()

    }

    override fun onEnable() {
        super.onEnable()
        logger.info("Plugin loaded!")

        /**
         * 实现功能Abel订阅
         */
        subscribeGroupMessages {
            // 翻译
            msgTranslateController.trigger(abelPluginController, this)

            // 川普
            msgTrumpController.trigger(abelPluginController, this)

            // 复读
            always {
                if (!abelPluginController.getStatus("复读", this.group.id)) {
                    return@always
                }
                if (msgRepeatController.contains(this.group.id)) {
                    // 更新msgRepeat内容
                    if (msgRepeatController[this.group.id]!!.update(this)) {
                        reply(msgRepeatController[this.group.id]!!.textBackRepeat(this.message, this.group))
                    }
                } else {
                    // 为本群创建一个msgRepeat
                    msgRepeatController[this.group.id] = MessagesRepeat(this)
                }
            }

            // 倒转GIF
            msgReverseGIF.trigger(abelPluginController, this)

            // 图片缩放
            msgImageResize.trigger(abelPluginController, this)
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
                    // 有没有被管理员禁用
                    if (!abelPluginController.adminGetStatus(i, this.group.id)) {
                        return@case
                    }
                    if (!abelPluginController.getStatus(i, this.group.id)) {
                            abelPluginController.enableFunc(i, this.group.id)
                            reply("不出意外的话。。咱打开${i}了")
                    } else {
                        abelPluginController.disableFunc(i, this.group.id)
                        reply("不出意外的话。。咱关掉${i}了")
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
            accept()
        }

    }

}