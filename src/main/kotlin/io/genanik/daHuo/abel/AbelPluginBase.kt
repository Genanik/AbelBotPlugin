package io.genanik.daHuo.abel

import net.mamoe.mirai.event.GroupMessageSubscribersBuilder

/**
 * 所有Abel插件的基类
 */
abstract class AbelPluginBase(aPlugins: AbelPlugins) {

    val abelPM = aPlugins

    abstract val name: String
    abstract val description: String
    abstract val version: String

    // TODO 临时不使用插件式编程
    init {
        abelPM.markPlugin(this)
    }

    open fun trigger(controller: GroupMessageSubscribersBuilder) {

    }

}