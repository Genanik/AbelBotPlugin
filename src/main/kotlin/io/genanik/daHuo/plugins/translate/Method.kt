package io.genanik.daHuo.plugins.translate

import com.github.houbb.opencc4j.util.ZhConverterUtil

class Method {
    fun localTranslate(query: String): String{
        return ZhConverterUtil.toSimple(query)
    }
}