package io.genanik.daHuo

import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.abel.regAbelDefault
import io.genanik.daHuo.abelCommand.*
import io.genanik.daHuo.plugins.*
import io.genanik.daHuo.utils.*
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.*

object AbelPluginMain : PluginBase() {

    // 为每个Abel插件创建对象
    private val msgRepeaterController = MessageRepeater()
    private val msgTranslateController = MessagesTranslate()
    private val msgTrumpController = DonaldTrump()
    private val msgReverseGIF = ReverseGIF()
    private val msgImageResize = ImageResize()
    private val timeController = Time()
    private val bilibiliPlugin = BilibiliMsg()

    private var abelPluginController = AbelPlugins(logger)

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
            msgRepeaterController.trigger(abelPluginController, this)

            // 倒转GIF
            msgReverseGIF.trigger(abelPluginController, this)

            // 图片缩放
            msgImageResize.trigger(abelPluginController, this)

            // bilibili
            bilibiliPlugin.trigger(abelPluginController, this)
        }

        // 注册Abel内容
        regAbelDefault(this, abelPluginController)

    }

}