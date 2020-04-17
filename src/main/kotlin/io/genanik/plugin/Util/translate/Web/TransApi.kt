package io.genanik.plugin.Util.translate.Web

import java.util.*

class TransApi(private val appid: String, private val securityKey: String) {

    fun getTransResult(query: String, from: String, to: String): String {
        val params = buildParams(query, from, to)
        return HttpGet.get(TRANS_API_HOST, params).toString()
    }

    private fun buildParams(
        query: String,
        from: String,
        to: String
    ): Map<String, String> {
        val params: MutableMap<String, String> = HashMap()
        params["q"] = query
        params["from"] = from
        params["to"] = to
        params["appid"] = appid
        // 随机数
        val salt = System.currentTimeMillis().toString()
        params["salt"] = salt
        // 签名
        val src = appid + query + salt + securityKey // 加密前的原文
        params["sign"] = MessageDigestUtils.md5(src).toString()
        return params
    }

    companion object {
        private const val TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate"
    }

}