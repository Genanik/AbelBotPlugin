package io.genanik.daHuo.plugins

import io.genanik.daHuo.abel.AbelPluginBase
import io.genanik.daHuo.abel.AbelPlugins
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull

/**
 * 三贱客名言
 */
class TheGrandTour(aPlugins: AbelPlugins) : AbelPluginBase(aPlugins) {

    private val sentence = arrayListOf<String>()
    private val keyWords = arrayListOf<String>()
    private val abelPM = aPlugins

    init {
        keyWords.add("来点TGT笑话")
        keyWords.add("来点TGT")
        keyWords.add("TGT笑话")
        keyWords.add("TGT")
        keyWords.add("三贱客")
        keyWords.add("三贱客笑话")
        keyWords.add("来点三贱客笑话")

        sentence.add("How hard can it be?")
        sentence.add("Poweeeeeer！Speeeeeeed!")
        sentence.add("Some say.....")
        sentence.add("All we know is，he's called the STIG")
        sentence.add("to the Ganbon")
        sentence.add("across the line!")
        sentence.add("On this bombshell, it's time to end. good night!")
        sentence.add("Still, could be worse.")
        sentence.add("Oh cock")
        sentence.add("二愣子才漂移")
        sentence.add("Clarkson!")
        sentence.add("Hammond!")
        sentence.add("I'm the driving GOD!")
    }

    // 三贱客
    override fun trigger(controller: GroupMessageSubscribersBuilder) {
        keyWords.forEach {
            controller.case(it) {
                // 是否开启
                if (!abelPM.getStatus("三贱客", this.group.id)) {
                    return@case
                }
                // 三贱客
                reply(sentence.random())
            }
        }

    }
}