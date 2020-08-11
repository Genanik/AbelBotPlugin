package io.genanik.daHuo.abel

import net.mamoe.mirai.event.GroupMessageSubscribersBuilder

/**
 * 所有Abel插件的基类
 */
open class AbelPluginBase(aPlugins: AbelPlugins) {

    private val abelPlugins = aPlugins

    open val description = ""
    open val version = ""

    init {

    }

    open fun trigger(controller: GroupMessageSubscribersBuilder) {

    }

}