package io.genanik.daHuo

import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.abel.regAbelDefault
import io.genanik.daHuo.abelCommand.*
import io.genanik.daHuo.adminPlugins.BastardBlocker
import io.genanik.daHuo.plugins.*
import io.genanik.daHuo.plugins.bilibili.BilibiliMsg
import io.genanik.daHuo.utils.*
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.*

object AbelPluginMain : PluginBase() {

    private var abelPluginController = AbelPlugins(logger)

    // 为每个Abel插件创建对象
    private val fuckBB = BastardBlocker()
    private val timeController = Time()

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
        fuckBB.onLoadWithBlocker()

    }

    override fun onEnable() {
        super.onEnable()
        logger.info("Plugin loaded!")

        /**
         * 实现功能Abel订阅
         */
        subscribeGroupMessages {
            // 注册所有功能
            abelPluginController.regAllPlugins(this)
        }

        // 枪毙
        fuckBB.trigger(abelPluginController, this)

        // 注册Abel内容
        regAbelDefault(this, abelPluginController)

    }

}