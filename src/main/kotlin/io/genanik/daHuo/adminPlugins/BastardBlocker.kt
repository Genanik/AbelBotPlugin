package io.genanik.daHuo.adminPlugins

import com.google.gson.Gson
import io.genanik.daHuo.AbelPluginMain
import io.genanik.daHuo.abel.AbelPlugins
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import java.io.File
import java.util.*

/**
 * 傻逼屏蔽器
 * 从最高级拒绝向下广播任何傻逼发的消息内容
 */
@Suppress("NonAsciiCharacters")
class BastardBlocker() {
    private val 傻逼们 = mutableListOf<Long>()

    fun trigger(abelPM: AbelPlugins, controller: AbelPluginMain) {
        // 处理傻逼
        /** 不接入abelPM，对付傻逼还要权限组干涉？就算是Abel管理员都他妈给你屏蔽了 */
        controller.subscribeMessages(priority = Listener.EventPriority.HIGHEST) {
            always {
                if (sender.id.是傻逼吗()) {
                    再你妈的见()
                }
            }
        }

        // 毙了谁
        abelPM.regAdminCommand("毙了谁"){
            val result = MessageChainBuilder()
            result.add(傻逼们.toString())
            return@regAdminCommand(result.asMessageChain())
        }

        // 添加傻逼
        abelPM.adminRegFunction("枪毙")
        controller.subscribeMessages {
            always {
                if (!abelPM.isAdmin(sender.id)){
                    return@always
                }
                val 新傻逼 = 解析新傻逼(message) ?: return@always
                if (有这个傻逼了吗(新傻逼)){
                    return@always
                }
                添加新傻逼(新傻逼)
                reply("毙了")
            }
        }
        // 想要删除傻逼？想的美
    }

    fun onLoadWithBlocker(){
        val file = File("AbelBlockList.list")
        if (!file.exists()){
            return // 傻逼们不存在
        }
        读取傻逼们()
    }

    private fun 读取傻逼们(){
        val raw = Gson().toJson(File("AbelBlockList.list").readText())
        val list = raw.split("\\n")

        list.forEach {
            val 傻逼 = it.replace("\"", "")
            try{
                傻逼们.add(傻逼.toLong())
            }catch (_: NumberFormatException){ }
        }
    }

    private fun 有这个傻逼了吗(这个傻逼: Long): Boolean {
        return 傻逼们.contains(这个傻逼)
    }

    private fun 保存傻逼们(){
        var result = ""
        傻逼们.forEach {
            result += "$it\n"
        }
        File("AbelBlockList.list").writeText(result)
    }

    private fun 解析新傻逼(msg: MessageChain): Long? {
        val tmp = msg.firstIsInstanceOrNull<PlainText>() ?: return null
        if (!tmp.content.contains("是傻逼")){ return null }
        val 傻逼格式 = """[0-9]+|(是傻逼)""".toRegex()
        val 新傻逼 = 傻逼格式.find(tmp.content) ?: return null
        return 新傻逼.groupValues[0].toLong()
    }

    private fun 添加新傻逼(新傻逼: Long) {
        傻逼们.add(新傻逼)
        保存傻逼们()
    }

    private fun Long.是傻逼吗(): Boolean {
        return 傻逼们.contains(this)
    }

    private fun Event.再你妈的见() {
        this.intercept()
    }
}