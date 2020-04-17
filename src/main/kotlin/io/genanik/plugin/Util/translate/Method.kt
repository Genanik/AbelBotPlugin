package io.genanik.plugin.Util.translate

import com.github.houbb.opencc4j.util.ZhConverterUtil
import io.genanik.plugin.Util.translate.Web.TransApi
import org.json.JSONException
import org.json.JSONObject


class Method {

    fun baiduTranslate(query: String): String{
        val api = TransApi(
            "20200318000400547",
            "nJygFQdnpsaaJepj_kFX"
        )
        var requestFanyiJson = JSONObject(api.getTransResult(query, "auto", "zh"))

        return try {
            "Error with code: " + requestFanyiJson.get("error_code")
        }catch (e: JSONException){
            val transResult = requestFanyiJson.getJSONArray("trans_result")
            transResult.getJSONObject(0).getString("dst")
        }
    }

    fun localTranslate(query: String): String{
        return ZhConverterUtil.toSimple(query)
    }
}