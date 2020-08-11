package io.genanik.daHuo.plugins

import io.genanik.daHuo.abel.AbelPluginBase
import io.genanik.daHuo.abel.AbelPlugins
import io.genanik.daHuo.utils.isHavePicture
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull

/**
 * 输入关键字，返回一条名人名言（伪
 */
class DonaldTrump(aPlugins: AbelPlugins) : AbelPluginBase(aPlugins) {

    override val name = "川普"
    override val description = "@Abel机器人并加上一个关键词，自动发送\"名人名言\""
    override val version = "0.1.0"

    private val sentence = arrayListOf<String>()
    private val taowa = arrayListOf<String>()

    init {
        sentence.add("我们有全球最好的/singleWord专家\n——特朗普")
        sentence.add("对于/singleWord，没什么需要恐慌的\n——特朗普")
        sentence.add("有些人会说我非常 非常非常有才，特别是/singleWord方面\n——特朗普")
        sentence.add("没有人比我特朗普更懂/singleWord")
        sentence.add("人们并没有真的从无到有创造出什么，而是重新组合创造出更多东西，例如/singleWord\n——特朗普")
        sentence.add("特朗普式/singleWord，欲盖而弥彰！")
        sentence.add("总统越是否认/singleWord，民众就越应该关心/singleWord")
        sentence.add("我认为/singleWord是不可避免的\n——特朗普")
        sentence.add("/singleWord并不可怕\n——特朗普")
        sentence.add("你不握手，怎么能是/singleWord呢？\n——特朗普")
        sentence.add("把/singleWord当作流感就好\n——特朗普")
        sentence.add("在网上搜索“/singleWord”，就会出现川建国的照片，建国很是不服。")
        sentence.add("关于“/singleWord”的新闻，都是FAKE NEWS!\n——特朗普")
        sentence.add("我们越来越明白，对人类文明威胁最大、破坏最惨的是/singleWord；其次是不受约束的权力。\n——特朗普")
        sentence.add("我们越来越明白，对人类文明威胁最大、破坏最惨的是/singleWord；其次是不受约束的权力。\n——特朗普")
        sentence.add("永不放弃，特别是/singleWord\n——特朗普")
        sentence.add("/singleWord用克莱因瓶喝水")
        sentence.add("/singleWord用克莱因瓶喝水")
        sentence.add("有一次/singleWord构建了一个所有集合构成的集合")
        sentence.add("谷歌曾经被迫从一个数据中心搬走，因为/singleWord不小心把索引压缩的太厉害导致产生了黑洞。")
        sentence.add("/singleWord的键盘没有按键，因为/singleWord可以掌控一切")
        sentence.add("我了解/singleWord, 我从这里赚了不少钱\n——特朗普")
        sentence.add("我会对/singleWord很好的\n——特朗普")
        sentence.add("我可能是第一个通过竞选总统来尝试/singleWord的\n——特朗普")
        sentence.add("兼顾幽默和生意，可以成就好/singleWord\n——特朗普")

        taowa.add("套娃")
//        taowa.add("禁止套娃")
//        taowa.add("禁止")
    }

    override fun trigger(controller: GroupMessageSubscribersBuilder){
        controller.atBot {
            // 是否开启
            if (!abelPM.getStatus("川普", this.group.id)) {
                return@atBot
            }
            if (isHavePicture(message)) {
                return@atBot
            }
            // 川普
            val tmp = message.firstIsInstanceOrNull<PlainText>()
            if (tmp != null) {
                val keyWord = tmp.content.replace(" ", "")
                if (keyWord != "") {
                    reply(trumpTextWithoutNPL(keyWord))
                }
            }
        }
    }


    private fun textStruct(singleWord: String): String{
        return sentence.random().replace("/singleWord", singleWord)
    }

    private fun trumpTextWithoutNPL(input: String): String {
        if (input.length > 5){
            return "这个关键词太长了_(:з」∠)_"
        }
        if (check(input)){
            return "禁止套娃！"
        }
        return textStruct(input)

    }

    private fun check(targetString: String): Boolean {
        var isTaoWa = false
        for (i in taowa){
            if (targetString.indexOf(i) != -1){
                isTaoWa = true
            }
        }
        return isTaoWa
    }

}