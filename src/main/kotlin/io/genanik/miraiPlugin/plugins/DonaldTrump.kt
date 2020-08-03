package io.genanik.miraiPlugin.plugins

/**
 * 输入关键字，返回一条名人名言（伪
 */
class DonaldTrump {

    var sentence = arrayListOf<String>()
    val taowa = arrayListOf<String>()

    init {
        taowa.add("套娃")
//        taowa.add("禁止套娃")
//        taowa.add("禁止")
    }

    private fun textStruct(singleWord: String): String{
        sentence.add("我们有全球最好的${singleWord}专家\n——特朗普")
        sentence.add("对于${singleWord}，没什么需要恐慌的\n——特朗普")
        sentence.add("有些人会说我非常 非常非常有才，特别是${singleWord}方面\n——特朗普")
        sentence.add("没有人比我特朗普更懂${singleWord}")
        sentence.add("人们并没有真的从无到有创造出什么，而是重新组合创造出更多东西，例如${singleWord}\n——特朗普")
        sentence.add("特朗普式${singleWord}，欲盖而弥彰！")
        sentence.add("总统越是否认${singleWord}，民众就越应该关心${singleWord}")
        sentence.add("我认为${singleWord}是不可避免的\n——特朗普")
        sentence.add("${singleWord}并不可怕\n——特朗普")
        sentence.add("你不握手，怎么能是${singleWord}呢？\n——特朗普")
        sentence.add("把${singleWord}当作流感就好\n——特朗普")
        val result = sentence.random()
        sentence = arrayListOf()
        return result
    }

    fun TrumpTextWithoutNPL(input: String): String {
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